package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;
    
    // 푸시 알림 설정
    @Column(nullable = false)
    private Boolean pushEnabled = true;
    
    @Column(nullable = false)
    private Boolean emailEnabled = false;
    
    // 알림 유형별 설정
    @Column(nullable = false)
    private Boolean transactionAlert = true;
    
    @Column(nullable = false)
    private Boolean budgetAlert = true;
    
    @Column(nullable = false)
    private Boolean goalAlert = true;
    
    @Column(nullable = false)
    private Boolean recurringAlert = true;
    
    @Column(nullable = false)
    private Boolean bookInviteAlert = true;
    
    @Column(nullable = false)
    private Boolean friendRequestAlert = true;
    
    @Column(nullable = false)
    private Boolean commentAlert = true;
    
    // 알림 시간 설정
    private String quietHoursStart; // HH:mm 형식
    
    private String quietHoursEnd; // HH:mm 형식
    
    @Column(nullable = false)
    private Boolean weekendAlert = true;
    
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
    public NotificationPreference(Member member) {
        this.member = member;
        // 기본값 설정
        this.pushEnabled = true;
        this.emailEnabled = false;
        this.transactionAlert = true;
        this.budgetAlert = true;
        this.goalAlert = true;
        this.recurringAlert = true;
        this.bookInviteAlert = true;
        this.friendRequestAlert = true;
        this.commentAlert = true;
        this.weekendAlert = true;
    }
} 