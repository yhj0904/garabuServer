package garabu.garabuServer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 자산 수정 요청 DTO
 */
@Schema(description = "자산 수정 요청")
public class UpdateAssetRequest {

    @Size(max = 100, message = "자산 이름은 100자 이하여야 합니다")
    @Schema(description = "자산 이름", example = "주거래 통장")
    private String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    @Schema(description = "자산 설명", example = "급여 받는 주거래 계좌")
    private String description;

    @Size(max = 50, message = "계좌번호는 50자 이하여야 합니다")
    @Schema(description = "계좌번호 (은행 계좌인 경우)", example = "110-123-456789")
    private String accountNumber;

    @Size(max = 50, message = "은행명은 50자 이하여야 합니다")
    @Schema(description = "은행명 (은행 계좌인 경우)", example = "신한은행")
    private String bankName;

    @Size(max = 50, message = "카드 타입은 50자 이하여야 합니다")
    @Schema(description = "카드 타입 (카드인 경우)", example = "신용카드")
    private String cardType;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    // 기본 생성자
    public UpdateAssetRequest() {}

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}