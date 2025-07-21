package garabu.garabuServer.repository;

import garabu.garabuServer.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    @Query("SELECT er FROM ExchangeRate er WHERE er.fromCurrency = :fromCurrency " +
           "AND er.toCurrency = :toCurrency AND er.rateDate = :rateDate")
    Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndRateDate(
            @Param("fromCurrency") String fromCurrency,
            @Param("toCurrency") String toCurrency,
            @Param("rateDate") LocalDate rateDate);
    
    @Query("SELECT er FROM ExchangeRate er WHERE er.fromCurrency = :fromCurrency " +
           "AND er.toCurrency = :toCurrency ORDER BY er.rateDate DESC LIMIT 1")
    Optional<ExchangeRate> findLatestRate(
            @Param("fromCurrency") String fromCurrency,
            @Param("toCurrency") String toCurrency);
    
    @Query("SELECT er FROM ExchangeRate er WHERE er.fromCurrency = :fromCurrency " +
           "AND er.toCurrency = :toCurrency AND er.rateDate BETWEEN :startDate AND :endDate " +
           "ORDER BY er.rateDate")
    List<ExchangeRate> findRateHistory(
            @Param("fromCurrency") String fromCurrency,
            @Param("toCurrency") String toCurrency,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT er FROM ExchangeRate er WHERE er.rateDate = :rateDate")
    List<ExchangeRate> findByRateDate(@Param("rateDate") LocalDate rateDate);
} 