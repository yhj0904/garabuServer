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

    @CacheEvict(value = {"paymentMethods", "paymentMethodsAll"}, allEntries = true)
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

}
