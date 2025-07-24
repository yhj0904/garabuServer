package garabu.garabuServer.repository;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentJpaRepository  extends JpaRepository<PaymentMethod, Long> {
    PaymentMethod findByPayment(String payment);
    
    // 가계부별 결제수단 조회
    List<PaymentMethod> findByBook(Book book);
    
    // 가계부별 결제수단명으로 조회
    PaymentMethod findByBookAndPayment(Book book, String payment);
    
    // 가계부별 결제수단 삭제
    void deleteByBook(Book book);
}
