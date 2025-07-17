package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 친구 관계 엔티티
 * 
 * 사용자 간의 친구 관계를 관리하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "친구 관계 엔티티")
public class Friendship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "친구 관계 ID")
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @Schema(description = "친구 요청자")
    private Member requester;  // 친구 요청을 보낸 사용자
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    @Schema(description = "친구 요청 대상자")
    private Member addressee;  // 친구 요청을 받은 사용자
    
    @Enumerated(EnumType.STRING)
    @Schema(description = "친구 관계 상태", example = "PENDING")
    private FriendshipStatus status;
    
    @Schema(description = "요청자가 설정한 친구 별칭", example = "아빠")
    private String requesterAlias;  // 요청자가 상대방에게 붙인 별칭
    
    @Schema(description = "대상자가 설정한 친구 별칭", example = "딸")
    private String addresseeAlias;  // 대상자가 요청자에게 붙인 별칭
    
    @Schema(description = "친구 요청 일시")
    private LocalDateTime requestedAt;
    
    @Schema(description = "친구 수락 일시")
    private LocalDateTime acceptedAt;
    
    @Schema(description = "마지막 상호작용 일시")
    private LocalDateTime lastInteractionAt;
    
    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        lastInteractionAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastInteractionAt = LocalDateTime.now();
    }
    
    /**
     * 친구 관계 상태를 수락으로 변경
     */
    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }
    
    /**
     * 친구 관계 상태를 거절로 변경
     */
    public void reject() {
        this.status = FriendshipStatus.REJECTED;
    }
    
    /**
     * 친구 관계 상태를 차단으로 변경
     */
    public void block() {
        this.status = FriendshipStatus.BLOCKED;
    }
    
    /**
     * 특정 사용자가 이 친구 관계에서 상대방인지 확인
     * 
     * @param member 확인할 사용자
     * @return 상대방 사용자 객체 또는 null
     */
    public Member getOtherMember(Member member) {
        if (requester.getId().equals(member.getId())) {
            return addressee;
        } else if (addressee.getId().equals(member.getId())) {
            return requester;
        }
        return null;
    }
    
    /**
     * 특정 사용자가 설정한 상대방 별칭 반환
     * 
     * @param member 확인할 사용자
     * @return 별칭 문자열
     */
    public String getAliasForMember(Member member) {
        if (requester.getId().equals(member.getId())) {
            return requesterAlias;
        } else if (addressee.getId().equals(member.getId())) {
            return addresseeAlias;
        }
        return null;
    }
    
    /**
     * 특정 사용자의 별칭 설정
     * 
     * @param member 설정할 사용자
     * @param alias 설정할 별칭
     */
    public void setAliasForMember(Member member, String alias) {
        if (requester.getId().equals(member.getId())) {
            this.requesterAlias = alias;
        } else if (addressee.getId().equals(member.getId())) {
            this.addresseeAlias = alias;
        }
    }
}