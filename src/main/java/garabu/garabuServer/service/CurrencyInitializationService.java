package garabu.garabuServer.service;

import garabu.garabuServer.domain.Currency;
import garabu.garabuServer.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurrencyInitializationService implements CommandLineRunner {
    
    private final CurrencyRepository currencyRepository;
    
    @Override
    @Transactional
    public void run(String... args) {
        log.info("기본 통화 데이터 초기화 시작");
        
        List<CurrencyData> defaultCurrencies = Arrays.asList(
            new CurrencyData("KRW", "Korean Won", "대한민국 원", "₩", 0),
            new CurrencyData("USD", "US Dollar", "미국 달러", "$", 2),
            new CurrencyData("EUR", "Euro", "유로", "€", 2),
            new CurrencyData("JPY", "Japanese Yen", "일본 엔", "¥", 0),
            new CurrencyData("CNY", "Chinese Yuan", "중국 위안", "¥", 2),
            new CurrencyData("GBP", "British Pound", "영국 파운드", "£", 2),
            new CurrencyData("CHF", "Swiss Franc", "스위스 프랑", "Fr", 2),
            new CurrencyData("CAD", "Canadian Dollar", "캐나다 달러", "$", 2),
            new CurrencyData("AUD", "Australian Dollar", "호주 달러", "$", 2),
            new CurrencyData("NZD", "New Zealand Dollar", "뉴질랜드 달러", "$", 2),
            new CurrencyData("HKD", "Hong Kong Dollar", "홍콩 달러", "$", 2),
            new CurrencyData("SGD", "Singapore Dollar", "싱가포르 달러", "$", 2),
            new CurrencyData("SEK", "Swedish Krona", "스웨덴 크로나", "kr", 2),
            new CurrencyData("NOK", "Norwegian Krone", "노르웨이 크로네", "kr", 2),
            new CurrencyData("DKK", "Danish Krone", "덴마크 크로네", "kr", 2),
            new CurrencyData("MXN", "Mexican Peso", "멕시코 페소", "$", 2),
            new CurrencyData("INR", "Indian Rupee", "인도 루피", "₹", 2),
            new CurrencyData("RUB", "Russian Ruble", "러시아 루블", "₽", 2),
            new CurrencyData("BRL", "Brazilian Real", "브라질 헤알", "R$", 2),
            new CurrencyData("ZAR", "South African Rand", "남아프리카 랜드", "R", 2),
            new CurrencyData("THB", "Thai Baht", "태국 바트", "฿", 2),
            new CurrencyData("MYR", "Malaysian Ringgit", "말레이시아 링깃", "RM", 2),
            new CurrencyData("IDR", "Indonesian Rupiah", "인도네시아 루피아", "Rp", 0),
            new CurrencyData("PHP", "Philippine Peso", "필리핀 페소", "₱", 2),
            new CurrencyData("VND", "Vietnamese Dong", "베트남 동", "₫", 0),
            new CurrencyData("TRY", "Turkish Lira", "터키 리라", "₺", 2),
            new CurrencyData("AED", "UAE Dirham", "아랍에미리트 디르함", "د.إ", 2),
            new CurrencyData("SAR", "Saudi Riyal", "사우디 리얄", "﷼", 2),
            new CurrencyData("PLN", "Polish Zloty", "폴란드 즈워티", "zł", 2),
            new CurrencyData("TWD", "Taiwan Dollar", "대만 달러", "$", 0)
        );
        
        for (CurrencyData data : defaultCurrencies) {
            if (!currencyRepository.existsByCurrencyCode(data.code)) {
                Currency currency = new Currency(
                    data.code,
                    data.name,
                    data.nameKr,
                    data.symbol,
                    data.decimalPlaces
                );
                currencyRepository.save(currency);
                log.info("통화 추가: {} - {}", data.code, data.nameKr);
            }
        }
        
        log.info("기본 통화 데이터 초기화 완료");
    }
    
    private static class CurrencyData {
        String code;
        String name;
        String nameKr;
        String symbol;
        Integer decimalPlaces;
        
        CurrencyData(String code, String name, String nameKr, String symbol, Integer decimalPlaces) {
            this.code = code;
            this.name = name;
            this.nameKr = nameKr;
            this.symbol = symbol;
            this.decimalPlaces = decimalPlaces;
        }
    }
} 