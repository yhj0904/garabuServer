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
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goal {
    
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
    private GoalType goalType;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate targetDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GoalStatus status = GoalStatus.ACTIVE;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 10)
    private String icon;
    
    @Column(length = 7)
    private String color;
    
    private LocalDateTime completedAt;
    
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
    
    // public 생성자 추가
    public static Goal createGoal(Book book, String name, GoalType goalType, BigDecimal targetAmount, LocalDate startDate, LocalDate targetDate) {
        Goal goal = new Goal();
        goal.book = book;
        goal.name = name;
        goal.goalType = goalType;
        goal.targetAmount = targetAmount;
        goal.startDate = startDate;
        goal.targetDate = targetDate;
        return goal;
    }
    
    // 비즈니스 메서드
    public void updateProgress(BigDecimal amount) {
        this.currentAmount = amount;
        checkAndUpdateStatus();
    }
    
    public void addProgress(BigDecimal amount) {
        this.currentAmount = this.currentAmount.add(amount);
        checkAndUpdateStatus();
    }
    
    private void checkAndUpdateStatus() {
        if (currentAmount.compareTo(targetAmount) >= 0 && status == GoalStatus.ACTIVE) {
            this.status = GoalStatus.COMPLETED;
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public BigDecimal getProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.multiply(new BigDecimal("100"))
                .divide(targetAmount, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public boolean isExpired() {
        return LocalDate.now().isAfter(targetDate) && status == GoalStatus.ACTIVE;
    }
} 