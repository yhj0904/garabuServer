package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transactions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurringTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmountType amountType;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurrenceType recurrenceType;
    
    @Column(nullable = false)
    private Integer recurrenceInterval = 1;
    
    @Column(length = 50)
    private String recurrenceDay; // 요일 또는 날짜
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private LocalDate nextExecutionDate;
    
    private LocalDate lastExecutionDate;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private Integer executionCount = 0;
    
    @Column
    private Integer maxExecutions;
    
    @Column(nullable = false)
    private Boolean autoCreate = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateNextExecutionDate();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // public 생성자 추가
    public static RecurringTransaction create(Book book, String name, AmountType amountType, 
                                            BigDecimal amount, RecurrenceType recurrenceType, 
                                            LocalDate startDate) {
        RecurringTransaction transaction = new RecurringTransaction();
        transaction.book = book;
        transaction.name = name;
        transaction.amountType = amountType;
        transaction.amount = amount;
        transaction.recurrenceType = recurrenceType;
        transaction.startDate = startDate;
        return transaction;
    }
    
    // 비즈니스 메서드
    public void execute() {
        this.lastExecutionDate = LocalDate.now();
        this.executionCount++;
        calculateNextExecutionDate();
        
        if (maxExecutions != null && executionCount >= maxExecutions) {
            this.isActive = false;
        }
    }
    
    public void pause() {
        this.isActive = false;
    }
    
    public void resume() {
        this.isActive = true;
        calculateNextExecutionDate();
    }
    
    private void calculateNextExecutionDate() {
        if (!isActive || (endDate != null && LocalDate.now().isAfter(endDate))) {
            this.nextExecutionDate = null;
            return;
        }
        
        LocalDate baseDate = lastExecutionDate != null ? lastExecutionDate : startDate;
        
        switch (recurrenceType) {
            case DAILY:
                this.nextExecutionDate = baseDate.plusDays(recurrenceInterval);
                break;
            case WEEKLY:
                this.nextExecutionDate = baseDate.plusWeeks(recurrenceInterval);
                break;
            case MONTHLY:
                this.nextExecutionDate = baseDate.plusMonths(recurrenceInterval);
                break;
            case YEARLY:
                this.nextExecutionDate = baseDate.plusYears(recurrenceInterval);
                break;
        }
        
        // 종료일이 있고 다음 실행일이 종료일 이후라면 비활성화
        if (endDate != null && nextExecutionDate.isAfter(endDate)) {
            this.nextExecutionDate = null;
            this.isActive = false;
        }
    }
    
    public boolean shouldExecute() {
        return isActive && 
               nextExecutionDate != null && 
               !LocalDate.now().isBefore(nextExecutionDate);
    }
} 