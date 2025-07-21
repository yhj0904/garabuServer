package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.currency.*;
import garabu.garabuServer.exception.BookAccessException;
import garabu.garabuServer.exception.BookNotFoundException;
import garabu.garabuServer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrencyService {
    
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final BookRepository bookRepository;
    private final UserBookJpaRepository userBookRepository;
    private final MemberService memberService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 외부 환율 API URL (예시)
    private static final String EXCHANGE_RATE_API_URL = "https://api.exchangerate-api.com/v4/latest/";
    
    @Cacheable("currencies")
    public List<CurrencyResponse> getAllCurrencies() {
        List<Currency> currencies = currencyRepository.findAllActiveCurrencies();
        
        return currencies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public ExchangeRateResponse getCurrentRate(String from, String to) {
        ExchangeRate rate = exchangeRateRepository.findLatestRate(from, to)
                .orElseThrow(() -> new IllegalArgumentException("환율 정보를 찾을 수 없습니다."));
        
        return convertToExchangeRateResponse(rate);
    }
    
    public List<ExchangeRateResponse> getHistoricalRates(String from, String to, LocalDate startDate, LocalDate endDate) {
        List<ExchangeRate> rates = exchangeRateRepository.findRateHistory(from, to, startDate, endDate);
        
        return rates.stream()
                .map(this::convertToExchangeRateResponse)
                .collect(Collectors.toList());
    }
    
    public CurrencyConversionResponse convert(CurrencyConversionRequest request) {
        ExchangeRate rate = exchangeRateRepository.findLatestRate(request.getFromCurrency(), request.getToCurrency())
                .orElseThrow(() -> new IllegalArgumentException("환율 정보를 찾을 수 없습니다."));
        
        BigDecimal convertedAmount = rate.convert(request.getAmount());
        
        CurrencyConversionResponse response = new CurrencyConversionResponse();
        response.setFromCurrency(request.getFromCurrency());
        response.setToCurrency(request.getToCurrency());
        response.setOriginalAmount(request.getAmount());
        response.setConvertedAmount(convertedAmount);
        response.setExchangeRate(rate.getRate());
        response.setRateDate(rate.getRateDate());
        
        return response;
    }
    
    @Transactional
    public BookCurrencyResponse updateBookCurrency(Long bookId, UpdateBookCurrencyRequest request) {
        Member currentMember = memberService.getCurrentMember();
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("가계부를 찾을 수 없습니다."));
        
        checkBookAccess(book, currentMember);
        
        // 통화 코드 유효성 확인
        currencyRepository.findByCurrencyCode(request.getCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 통화 코드입니다."));
        
        book.setDefaultCurrency(request.getCurrencyCode());
        book.setUseMultiCurrency(request.getUseMultiCurrency());
        
        Book updatedBook = bookRepository.save(book);
        
        BookCurrencyResponse response = new BookCurrencyResponse();
        response.setBookId(updatedBook.getId());
        response.setDefaultCurrency(updatedBook.getDefaultCurrency());
        response.setUseMultiCurrency(updatedBook.getUseMultiCurrency());
        
        return response;
    }
    
    @Transactional
    public void updateExchangeRate(UpdateExchangeRateRequest request) {
        ExchangeRate existingRate = exchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(
                        request.getFromCurrency(),
                        request.getToCurrency(),
                        request.getRateDate()
                )
                .orElse(null);
        
        if (existingRate != null) {
            existingRate.setRate(request.getRate());
            exchangeRateRepository.save(existingRate);
        } else {
            ExchangeRate newRate = new ExchangeRate(
                    request.getFromCurrency(),
                    request.getToCurrency(),
                    request.getRate(),
                    request.getRateDate(),
                    "manual"
            );
            exchangeRateRepository.save(newRate);
        }
    }
    
    // 매일 오전 9시에 환율 정보 업데이트
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void updateExchangeRatesFromExternalAPI() {
        log.info("환율 정보 업데이트 시작");
        
        List<Currency> activeCurrencies = currencyRepository.findAllActiveCurrencies();
        LocalDate today = LocalDate.now();
        
        for (Currency baseCurrency : activeCurrencies) {
            try {
                // 외부 API 호출 (예시)
                String url = EXCHANGE_RATE_API_URL + baseCurrency.getCurrencyCode();
                ExchangeRateApiResponse apiResponse = restTemplate.getForObject(url, ExchangeRateApiResponse.class);
                
                if (apiResponse != null && apiResponse.getRates() != null) {
                    for (Currency targetCurrency : activeCurrencies) {
                        if (!baseCurrency.getCurrencyCode().equals(targetCurrency.getCurrencyCode())) {
                            BigDecimal rate = apiResponse.getRates().get(targetCurrency.getCurrencyCode());
                            if (rate != null) {
                                saveOrUpdateExchangeRate(
                                        baseCurrency.getCurrencyCode(),
                                        targetCurrency.getCurrencyCode(),
                                        rate,
                                        today,
                                        "exchangerate-api.com"
                                );
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("환율 정보 업데이트 실패: {}", baseCurrency.getCurrencyCode(), e);
            }
        }
        
        log.info("환율 정보 업데이트 완료");
    }
    
    private void saveOrUpdateExchangeRate(String from, String to, BigDecimal rate, LocalDate date, String source) {
        ExchangeRate existingRate = exchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(from, to, date)
                .orElse(null);
        
        if (existingRate != null) {
            existingRate.setRate(rate);
            existingRate.setSource(source);
            exchangeRateRepository.save(existingRate);
        } else {
            ExchangeRate newRate = new ExchangeRate(from, to, rate, date, source);
            exchangeRateRepository.save(newRate);
        }
    }
    
    private void checkBookAccess(Book book, Member member) {
        boolean hasAccess = userBookRepository.findByBookIdAndMemberId(book.getId(), member.getId())
                .isPresent();
        
        if (!hasAccess) {
            throw new BookAccessException("이 가계부에 접근할 권한이 없습니다.");
        }
    }
    
    private CurrencyResponse convertToResponse(Currency currency) {
        CurrencyResponse response = new CurrencyResponse();
        response.setId(currency.getId());
        response.setCurrencyCode(currency.getCurrencyCode());
        response.setCurrencyName(currency.getCurrencyName());
        response.setCurrencyNameKr(currency.getCurrencyNameKr());
        response.setSymbol(currency.getSymbol());
        response.setDecimalPlaces(currency.getDecimalPlaces());
        
        return response;
    }
    
    private ExchangeRateResponse convertToExchangeRateResponse(ExchangeRate rate) {
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setFromCurrency(rate.getFromCurrency());
        response.setToCurrency(rate.getToCurrency());
        response.setRate(rate.getRate());
        response.setRateDate(rate.getRateDate());
        response.setSource(rate.getSource());
        
        return response;
    }
    
    // 외부 API 응답 DTO (예시)
    @lombok.Data
    private static class ExchangeRateApiResponse {
        private String base;
        private String date;
        private java.util.Map<String, BigDecimal> rates;
    }
}