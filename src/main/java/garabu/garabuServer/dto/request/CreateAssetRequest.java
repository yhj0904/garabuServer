package garabu.garabuServer.dto.request;

import garabu.garabuServer.domain.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 자산 생성 요청 DTO
 */
@Schema(description = "자산 생성 요청")
public class CreateAssetRequest {

    @NotBlank(message = "자산 이름은 필수입니다")
    @Size(max = 100, message = "자산 이름은 100자 이하여야 합니다")
    @Schema(description = "자산 이름", example = "주거래 통장")
    private String name;

    @NotNull(message = "자산 타입은 필수입니다")
    @Schema(description = "자산 타입", example = "SAVINGS_ACCOUNT")
    private AssetType assetType;

    @NotNull(message = "초기 잔액은 필수입니다")
    @PositiveOrZero(message = "초기 잔액은 0 이상이어야 합니다")
    @Schema(description = "초기 잔액", example = "1000000")
    private Long balance;

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

    // 기본 생성자
    public CreateAssetRequest() {}

    // 생성자
    public CreateAssetRequest(String name, AssetType assetType, Long balance) {
        this.name = name;
        this.assetType = assetType;
        this.balance = balance;
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
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
}