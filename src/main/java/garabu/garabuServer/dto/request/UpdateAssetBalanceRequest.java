package garabu.garabuServer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * 자산 잔액 수정 요청 DTO
 */
@Schema(description = "자산 잔액 수정 요청")
public class UpdateAssetBalanceRequest {

    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 양수여야 합니다")
    @Schema(description = "변경할 금액", example = "50000")
    private Long amount;

    @NotNull(message = "연산 타입은 필수입니다")
    @Pattern(regexp = "^(ADD|SUBTRACT)$", message = "연산 타입은 ADD 또는 SUBTRACT만 가능합니다")
    @Schema(description = "연산 타입 (ADD: 추가, SUBTRACT: 차감)", example = "ADD")
    private String operation;

    // 기본 생성자
    public UpdateAssetBalanceRequest() {}

    // 생성자
    public UpdateAssetBalanceRequest(Long amount, String operation) {
        this.amount = amount;
        this.operation = operation;
    }

    // Getter and Setter methods
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}