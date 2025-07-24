package garabu.garabuServer.dto;

import garabu.garabuServer.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 결제 수단 DTO
 * Redis 캐싱과 API 응답에 사용되는 데이터 전송 객체
 * 
 * JPA 엔티티의 Hibernate 프록시 객체와 연관관계로 인한 
 * 직렬화 문제를 해결하기 위해 단순한 값 객체로 설계
 */
@Data
@NoArgsConstructor
@Schema(description = "결제 수단 DTO")
public class PaymentMethodDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "결제 수단 ID", example = "1")
    private Long id;
    
    @Schema(description = "결제 수단명", example = "현금")
    private String payment;
    
    @Schema(description = "소속 가계부 ID", example = "5")
    private Long bookId;
    
    @Schema(description = "연결된 자산 ID", example = "10")
    private Long assetId;
    
    @Schema(description = "연결된 자산명", example = "현금")
    private String assetName;
    
    @Schema(description = "자산 잔액", example = "50000")
    private Long assetBalance;
    
    /**
     * JPA 엔티티로부터 DTO 생성
     */
    public PaymentMethodDto(PaymentMethod entity) {
        this.id = entity.getId();
        this.payment = entity.getPayment();
        this.bookId = entity.getBook() != null ? entity.getBook().getId() : null;
        if (entity.getAsset() != null) {
            this.assetId = entity.getAsset().getId();
            this.assetName = entity.getAsset().getName();
            this.assetBalance = entity.getAsset().getBalance();
        }
    }
    
    /**
     * 편의 생성자
     */
    public PaymentMethodDto(Long id, String payment, Long bookId) {
        this.id = id;
        this.payment = payment;
        this.bookId = bookId;
    }
    
    /**
     * 엔티티에서 DTO로 변환하는 정적 팩토리 메서드
     */
    public static PaymentMethodDto from(PaymentMethod entity) {
        return new PaymentMethodDto(entity);
    }
}