package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 가계부 참가 요청을 관리하는 엔티티
 * 사용자가 초대 코드를 통해 가계부에 참가 요청을 하면 생성되며,
 * OWNER가 수락/반려할 수 있습니다.
 */
@Entity
@Table(name = "user_book_request")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookRole requestedRole = BookRole.VIEWER;
    
    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;
    
    @Column(name = "response_date")
    private LocalDateTime responseDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private Member respondedBy;
    
    @Column(name = "invite_code", length = 8)
    private String inviteCode;
    
    @Column(name = "group_name")
    private String groupName;
    
    @PrePersist
    public void prePersist() {
        this.requestDate = LocalDateTime.now();
    }
} 