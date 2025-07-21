package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.dto.PaymentMethodDto;
import garabu.garabuServer.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentJpaRepository paymentJpaRepository;

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
    @CacheEvict(value = {"paymentMethodsByBookDto"}, key = "#book.id")
    public Long createPaymentForBook(Book book, String paymentName) {
        PaymentMethod payment = new PaymentMethod();
        payment.setPayment(paymentName);
        payment.setBook(book);
        paymentJpaRepository.save(payment);
        return payment.getId();
    }
}
