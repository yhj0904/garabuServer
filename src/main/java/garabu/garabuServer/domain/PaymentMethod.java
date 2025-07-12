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
@Table(name = "payment")
@Getter @Setter
@Schema(description = "결제수단 엔티티")
public class PaymentMethod {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    @Schema(description = "결제수단 ID")
    private Long id;        //결제수단 식별자

    @Schema(description = "결제수단명", example = "현금")
    private String payment;  //현금, 계좌이체, 카드 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner", "userBooks"})
    @Schema(description = "소속 가계부")
    private Book book;      // 가계부 ID
}
