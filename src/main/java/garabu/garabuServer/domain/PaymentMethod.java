package garabu.garabuServer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 결제수단 엔티티
 *
 * 현금, 계좌이체, 카드 등 결제수단 정보를 관리하는 JPA 엔티티입니다.
 * 각 가계부별로 독립적인 결제수단을 가집니다.
 */
@Entity
@Table(name = "payment",
       indexes = {
           @Index(name = "idx_payment_book_id", columnList = "book_id")
       })
@Getter @Setter
@Schema(description = "결제수단 엔티티")
public class PaymentMethod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    @Schema(description = "결제수단 ID")
    private Long id;

    @Schema(description = "결제수단명", example = "현금")
    private String payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_payment_book"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner", "userBooks"})
    @Schema(description = "소속 가계부")
    private Book book;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", foreignKey = @ForeignKey(name = "fk_payment_asset"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(description = "연결된 자산")
    private Asset asset;

    // 기본 생성자
    public PaymentMethod() {}

    // 생성자
    public PaymentMethod(String payment, Book book) {
        this.payment = payment;
        this.book = book;
    }
    
    // 자산 포함 생성자
    public PaymentMethod(String payment, Book book, Asset asset) {
        this.payment = payment;
        this.book = book;
        this.asset = asset;
    }
}
