package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentJpaRepository paymentJpaRepository;

    public Long registPayment(PaymentMethod payment){

        paymentJpaRepository.save(payment);
        return payment.getId();
    }
    public List<PaymentMethod> findAllPayment() {
        return paymentJpaRepository.findAll();
    }

    public PaymentMethod findById(Long id) {
        return paymentJpaRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public PaymentMethod findByPayment(String name) {
        return paymentJpaRepository.findByPayment(name);
    }

}
