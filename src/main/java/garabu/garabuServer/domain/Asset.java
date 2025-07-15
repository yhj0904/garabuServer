package garabu.garabuServer.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 자산 엔티티
 * 사용자의 실제 자산(현금, 계좌, 카드 등)을 관리
 */
@Entity
@Table(name = "asset")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private Long id;

    @Column(name = "asset_name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    @Column(name = "balance", nullable = false)
    private Long balance = 0L;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    @Column(name = "card_type", length = 50)
    private String cardType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 기본 생성자
    protected Asset() {}

    // 생성자
    public Asset(String name, AssetType assetType, Long balance, Book book) {
        this.name = name;
        this.assetType = assetType;
        this.balance = balance;
        this.book = book;
    }

    // 잔액 업데이트 메서드
    public void updateBalance(Long amount, String operation) {
        if ("ADD".equals(operation)) {
            this.balance += amount;
        } else if ("SUBTRACT".equals(operation)) {
            this.balance -= amount;
        }
        
        // 잔액이 음수가 되지 않도록 보장 (신용카드 제외)
        if (this.balance < 0 && this.assetType != AssetType.CREDIT_CARD) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
    }

    // 정보 업데이트 메서드
    public void updateInfo(String name, String description, String accountNumber, 
                          String bankName, String cardType) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.cardType = cardType;
    }

    // 활성화/비활성화
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // Getter methods
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public Long getBalance() {
        return balance;
    }

    public String getDescription() {
        return description;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public String getCardType() {
        return cardType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Book getBook() {
        return book;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setter methods (필요한 경우에만)
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}