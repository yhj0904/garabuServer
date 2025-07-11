package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentJpaRepository paymentJpaRepository;

    @CacheEvict(value = {"paymentMethods", "paymentMethodsAll", "paymentMethodsByBook"}, allEntries = true)
    public Long registPayment(PaymentMethod payment){

        paymentJpaRepository.save(payment);
        return payment.getId();
    }
    
    @Cacheable(value = "paymentMethodsAll", unless = "#result == null or #result.isEmpty()")
    public List<PaymentMethod> findAllPayment() {
        return paymentJpaRepository.findAll();
    }

    @Cacheable(value = "paymentMethods", key = "#id", unless = "#result == null")
    public PaymentMethod findById(Long id) {
        return paymentJpaRepository.findById(id).orElseThrow(() -> new RuntimeException("PaymentMethod not found"));
    }

    @Cacheable(value = "paymentMethods", key = "#name", unless = "#result == null")
    public PaymentMethod findByPayment(String name) {
        return paymentJpaRepository.findByPayment(name);
    }

    // 가계부별 결제수단 조회
    @Cacheable(value = "paymentMethodsByBook", key = "#book.id", unless = "#result == null or #result.isEmpty()")
    public List<PaymentMethod> findByBook(Book book) {
        return paymentJpaRepository.findByBook(book);
    }

    // 가계부별 결제수단명으로 조회
    public PaymentMethod findByBookAndPayment(Book book, String payment) {
        return paymentJpaRepository.findByBookAndPayment(book, payment);
    }

    // 가계부별 결제수단 생성
    @CacheEvict(value = {"paymentMethodsByBook"}, key = "#book.id")
    public Long createPaymentForBook(Book book, String paymentName) {
        PaymentMethod payment = new PaymentMethod();
        payment.setPayment(paymentName);
        payment.setBook(book);
        paymentJpaRepository.save(payment);
        return payment.getId();
    }
}
