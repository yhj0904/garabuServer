package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "currencies",
    indexes = {
        @Index(name = "idx_currency_code", columnList = "currencyCode", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Currency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 3)
    private String currencyCode; // ISO 4217 코드 (예: USD, KRW)
    
    @Column(nullable = false, length = 50)
    private String currencyName;
    
    @Column(nullable = false, length = 50)
    private String currencyNameKr;
    
    @Column(nullable = false, length = 10)
    private String symbol;
    
    @Column(nullable = false)
    private Integer decimalPlaces;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
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
    public Currency(String currencyCode, String currencyName, String currencyNameKr, 
                   String symbol, Integer decimalPlaces) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.currencyNameKr = currencyNameKr;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
        this.isActive = true;
    }
} 