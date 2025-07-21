package garabu.garabuServer.repository;

import garabu.garabuServer.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    Optional<Currency> findByCurrencyCode(String currencyCode);
    
    List<Currency> findByIsActiveTrue();
    
    @Query("SELECT c FROM Currency c WHERE c.isActive = true ORDER BY c.currencyCode")
    List<Currency> findAllActiveCurrencies();
    
    boolean existsByCurrencyCode(String currencyCode);
} 