package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 메시지 엔티티
 * 
 * 친구 간 메시지를 관리하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "메시지 엔티티")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "메시지 ID")
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @Schema(description = "메시지 발신자")
    private Member sender;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    @Schema(description = "메시지 수신자")
    private Member receiver;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "friendship_id", nullable = false)
    @Schema(description = "친구 관계")
    private Friendship friendship;
    
    @Enumerated(EnumType.STRING)
    @Schema(description = "메시지 타입", example = "TEXT")
    private MessageType messageType;
    
    @Column(columnDefinition = "TEXT")
    @Schema(description = "메시지 내용")
    private String content;
    
    @Schema(description = "첨부파일 URL")
    private String attachmentUrl;
    
    @Schema(description = "첨부파일 타입")
    private String attachmentType;
    
    @Schema(description = "메시지 상태", example = "SENT")
    @Enumerated(EnumType.STRING)
    private MessageStatus status;
    
    @Schema(description = "메시지 전송 일시")
    private LocalDateTime sentAt;
    
    @Schema(description = "메시지 읽음 일시")
    private LocalDateTime readAt;
    
    @Schema(description = "메시지 삭제 일시")
    private LocalDateTime deletedAt;
    
    @Schema(description = "발신자 측 삭제 여부")
    private boolean deletedBySender = false;
    
    @Schema(description = "수신자 측 삭제 여부")
    private boolean deletedByReceiver = false;
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (status == null) {
            status = MessageStatus.SENT;
        }
    }
    
    /**
     * 메시지 읽음 처리
     */
    public void markAsRead() {
        this.status = MessageStatus.READ;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * 메시지 삭제 처리
     * 
     * @param deletedBy 삭제한 사용자
     */
    public void deleteForUser(Member deletedBy) {
        if (sender.getId().equals(deletedBy.getId())) {
            this.deletedBySender = true;
        } else if (receiver.getId().equals(deletedBy.getId())) {
            this.deletedByReceiver = true;
        }
        
        // 양쪽 모두 삭제했으면 전체 삭제
        if (deletedBySender && deletedByReceiver) {
            this.deletedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 특정 사용자에게 메시지가 보이는지 확인
     * 
     * @param member 확인할 사용자
     * @return 메시지 표시 여부
     */
    public boolean isVisibleToUser(Member member) {
        if (deletedAt != null) {
            return false;
        }
        
        if (sender.getId().equals(member.getId())) {
            return !deletedBySender;
        } else if (receiver.getId().equals(member.getId())) {
            return !deletedByReceiver;
        }
        
        return false;
    }
}