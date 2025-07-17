package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 친구 그룹 멤버 엔티티
 * 
 * 친구 그룹과 친구 관계를 연결하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "친구 그룹 멤버 엔티티")
public class FriendGroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "친구 그룹 멤버 ID")
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "friend_group_id", nullable = false)
    @Schema(description = "친구 그룹")
    private FriendGroup friendGroup;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "friendship_id", nullable = false)
    @Schema(description = "친구 관계")
    private Friendship friendship;
    
    @Schema(description = "그룹에 추가된 일시")
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}