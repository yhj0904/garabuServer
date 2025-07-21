package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "budget_alerts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetAlert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false)
    private Integer alertThreshold; // 알림 임계값 (%)
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private LocalDateTime lastAlertSentAt;
    
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
    public BudgetAlert(Budget budget, Member member, Integer alertThreshold) {
        this.budget = budget;
        this.member = member;
        this.alertThreshold = alertThreshold;
    }
    
    // 비즈니스 메서드
    public boolean shouldSendAlert(double currentUsagePercentage) {
        if (!isActive) {
            return false;
        }
        
        // 임계값을 초과하고, 마지막 알림 후 24시간이 지났거나 처음 알림인 경우
        if (currentUsagePercentage >= alertThreshold) {
            if (lastAlertSentAt == null || 
                lastAlertSentAt.plusHours(24).isBefore(LocalDateTime.now())) {
                return true;
            }
        }
        
        return false;
    }
    
    public void markAlertSent() {
        this.lastAlertSentAt = LocalDateTime.now();
    }
} 