package garabu.garabuServer.repository;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository  extends JpaRepository<PaymentMethod, Long> {
    PaymentMethod findByName(String name);
}
