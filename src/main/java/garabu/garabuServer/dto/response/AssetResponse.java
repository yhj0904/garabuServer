package garabu.garabuServer.dto.response;

import garabu.garabuServer.domain.Asset;
import garabu.garabuServer.domain.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 자산 응답 DTO
 */
@Schema(description = "자산 응답")
public class AssetResponse {

    @Schema(description = "자산 ID", example = "1")
    private Long id;

    @Schema(description = "자산 이름", example = "주거래 통장")
    private String name;

    @Schema(description = "자산 타입", example = "SAVINGS_ACCOUNT")
    private AssetType assetType;

    @Schema(description = "자산 타입 표시명", example = "저축예금")
    private String assetTypeName;

    @Schema(description = "잔액", example = "1000000")
    private Long balance;

    @Schema(description = "자산 설명", example = "급여 받는 주거래 계좌")
    private String description;

    @Schema(description = "계좌번호", example = "110-123-456789")
    private String accountNumber;

    @Schema(description = "은행명", example = "신한은행")
    private String bankName;

    @Schema(description = "카드 타입", example = "신용카드")
    private String cardType;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "가계부 ID", example = "1")
    private Long bookId;

    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-01T00:00:00")
    private LocalDateTime updatedAt;

    // 기본 생성자
    public AssetResponse() {}

    // Asset 엔티티로부터 생성하는 생성자
    public AssetResponse(Asset asset) {
        this.id = asset.getId();
        this.name = asset.getName();
        this.assetType = asset.getAssetType();
        this.assetTypeName = asset.getAssetType().getDisplayName();
        this.balance = asset.getBalance();
        this.description = asset.getDescription();
        this.accountNumber = asset.getAccountNumber();
        this.bankName = asset.getBankName();
        this.cardType = asset.getCardType();
        this.isActive = asset.getIsActive();
        this.bookId = asset.getBook().getId();
        this.createdAt = asset.getCreatedAt();
        this.updatedAt = asset.getUpdatedAt();
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getAssetTypeName() {
        return assetTypeName;
    }

    public void setAssetTypeName(String assetTypeName) {
        this.assetTypeName = assetTypeName;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}