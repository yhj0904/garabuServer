package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "receipt_items")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReceiptItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;
    
    @Column(nullable = false, length = 200)
    private String itemName;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(length = 50)
    private String category;
    
    // 생성자
    public ReceiptItem(String itemName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }
    
    // 비즈니스 메서드
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = quantity.multiply(unitPrice);
        }
    }
} 