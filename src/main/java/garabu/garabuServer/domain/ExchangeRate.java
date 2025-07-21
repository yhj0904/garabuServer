package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates",
    indexes = {
        @Index(name = "idx_exchange_rate_date", columnList = "rateDate, fromCurrency, toCurrency"),
        @Index(name = "idx_exchange_rate_currencies", columnList = "fromCurrency, toCurrency")
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 3)
    private String fromCurrency;
    
    @Column(nullable = false, length = 3)
    private String toCurrency;
    
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;
    
    @Column(nullable = false)
    private LocalDate rateDate;
    
    @Column(length = 50)
    private String source; // 환율 정보 출처
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 생성자
    public ExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, 
                       LocalDate rateDate, String source) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.rateDate = rateDate;
        this.source = source;
    }
    
    // 비즈니스 메서드
    public BigDecimal convert(BigDecimal amount) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
} 