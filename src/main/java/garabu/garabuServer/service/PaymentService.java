package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.domain.Asset;
import garabu.garabuServer.domain.AssetType;
import garabu.garabuServer.dto.PaymentMethodDto;
import garabu.garabuServer.repository.PaymentJpaRepository;
import garabu.garabuServer.repository.AssetJpaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final PaymentJpaRepository paymentJpaRepository;
    private final AssetJpaRepository assetJpaRepository;

    @CacheEvict(value = {"paymentMethodsByBookDto"}, allEntries = true)
    public Long registPayment(PaymentMethod payment){

        paymentJpaRepository.save(payment);
        return payment.getId();
    }
    
    // Deprecated: Entity caching removed to prevent LazyInitializationException
    public List<PaymentMethod> findAllPayment() {
        return paymentJpaRepository.findAll();
    }

    // Deprecated: Entity caching removed to prevent LazyInitializationException
    public PaymentMethod findById(Long id) {
        return paymentJpaRepository.findById(id).orElseThrow(() -> new RuntimeException("PaymentMethod not found"));
    }

    // Deprecated: Entity caching removed to prevent LazyInitializationException
    public PaymentMethod findByPayment(String name) {
        return paymentJpaRepository.findByPayment(name);
    }

    // 가계부별 결제수단 조회 (DTO 반환)
    @Cacheable(value = "paymentMethodsByBookDto", key = "#book.id", unless = "#result == null or #result.isEmpty()")
    public List<PaymentMethodDto> findByBookDto(Book book) {
        List<PaymentMethod> entities = paymentJpaRepository.findByBook(book);
        return entities.stream()
                .map(PaymentMethodDto::from)
                .collect(Collectors.toList());
    }
    
    // 캐시 클리어 메서드
    @CacheEvict(value = "paymentMethodsByBookDto", allEntries = true)
    public void clearAllPaymentCaches() {
        // 모든 PaymentMethod 캐시를 클리어합니다.
        logger.info("PaymentMethod 캐시를 모두 클리어했습니다.");
    }
    
    // 특정 가계부 PaymentMethod 캐시 클리어
    @CacheEvict(value = "paymentMethodsByBookDto", key = "#book.id")
    public void clearPaymentCacheByBook(Book book) {
        logger.info("가계부 ID {} PaymentMethod 캐시를 클리어했습니다.", book.getId());
    }
    
    // 기존 엔티티 반환 메서드
    // Deprecated: Entity caching removed to prevent LazyInitializationException
    // Use findByBookDto() instead
    public List<PaymentMethod> findByBook(Book book) {
        return paymentJpaRepository.findByBook(book);
    }

    // 가계부별 결제수단명으로 조회
    public PaymentMethod findByBookAndPayment(Book book, String payment) {
        return paymentJpaRepository.findByBookAndPayment(book, payment);
    }

    // 가계부별 결제수단 생성
    @Transactional(rollbackFor = Exception.class, timeout = 15)
    @CacheEvict(value = {"paymentMethodsByBookDto"}, key = "#book.id")
    public Long createPaymentForBook(Book book, String paymentName) {
        // 1. 자산 생성
        Asset asset = new Asset(paymentName, determineAssetType(paymentName), 0L, book);
        assetJpaRepository.save(asset);
        
        // 2. 결제수단 생성 (자산과 연결)
        PaymentMethod payment = new PaymentMethod();
        payment.setPayment(paymentName);
        payment.setBook(book);
        payment.setAsset(asset);
        paymentJpaRepository.save(payment);
        
        return payment.getId();
    }
    
    // 결제수단명으로 자산 타입 결정
    private AssetType determineAssetType(String paymentName) {
        String lowerName = paymentName.toLowerCase();
        
        if (lowerName.contains("현금") || lowerName.contains("cash")) {
            return AssetType.CASH;
        } else if (lowerName.contains("신용") || lowerName.contains("credit")) {
            return AssetType.CREDIT_CARD;
        } else if (lowerName.contains("체크") || lowerName.contains("debit") || lowerName.contains("직불")) {
            return AssetType.DEBIT_CARD;
        } else if (lowerName.contains("저축") || lowerName.contains("saving")) {
            return AssetType.SAVINGS_ACCOUNT;
        } else if (lowerName.contains("당좌") || lowerName.contains("checking")) {
            return AssetType.CHECKING_ACCOUNT;
        } else if (lowerName.contains("계좌") || lowerName.contains("통장") || lowerName.contains("은행") || lowerName.contains("account")) {
            // 일반 계좌는 저축예금으로 분류
            return AssetType.SAVINGS_ACCOUNT;
        } else if (lowerName.contains("투자") || lowerName.contains("invest")) {
            return AssetType.INVESTMENT;
        } else {
            // 기본값은 현금으로 설정
            return AssetType.CASH;
        }
    }
}
