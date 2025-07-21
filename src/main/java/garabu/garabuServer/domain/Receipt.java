package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receipts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member uploadedBy;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String fileType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(length = 100)
    private String storeName;
    
    private LocalDate purchaseDate;
    
    private Integer totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReceiptStatus status = ReceiptStatus.UPLOADED;
    
    @Column(columnDefinition = "TEXT")
    private String ocrResult;
    
    @Column(columnDefinition = "TEXT")
    private String ocrRawData;
    
    private LocalDateTime ocrProcessedAt;
    
    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceiptItem> items = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "receipt_ledgers",
        joinColumns = @JoinColumn(name = "receipt_id"),
        inverseJoinColumns = @JoinColumn(name = "ledger_id")
    )
    private List<Ledger> linkedLedgers = new ArrayList<>();
    
    @Column(length = 500)
    private String memo;
    
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
    public Receipt(Book book, Member uploadedBy, String fileName, String filePath, String fileType, Long fileSize) {
        this.book = book;
        this.uploadedBy = uploadedBy;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }
    
    // 비즈니스 메서드
    public void updateOcrResult(String ocrResult, String ocrRawData) {
        this.ocrResult = ocrResult;
        this.ocrRawData = ocrRawData;
        this.ocrProcessedAt = LocalDateTime.now();
        this.status = ReceiptStatus.OCR_COMPLETED;
    }
    
    public void markAsProcessed() {
        this.status = ReceiptStatus.PROCESSED;
    }
    
    public void markAsFailed() {
        this.status = ReceiptStatus.OCR_FAILED;
    }
    
    public void addItem(ReceiptItem item) {
        items.add(item);
        item.setReceipt(this);
    }
    
    public void removeItem(ReceiptItem item) {
        items.remove(item);
        item.setReceipt(null);
    }
    
    public void linkLedger(Ledger ledger) {
        if (!linkedLedgers.contains(ledger)) {
            linkedLedgers.add(ledger);
        }
    }
    
    public void unlinkLedger(Ledger ledger) {
        linkedLedgers.remove(ledger);
    }
} 