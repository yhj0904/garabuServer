package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 친구 그룹 엔티티
 * 
 * 사용자가 생성한 친구 그룹을 관리하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "친구 그룹 엔티티")
public class FriendGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "친구 그룹 ID")
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @Schema(description = "그룹 소유자")
    private Member owner;  // 그룹을 생성한 사용자
    
    @Schema(description = "그룹명", example = "가족그룹")
    private String name;
    
    @Schema(description = "그룹 설명", example = "우리 가족 그룹입니다")
    private String description;
    
    @Schema(description = "그룹 색상", example = "#FF6B6B")
    private String color;
    
    @Schema(description = "그룹 아이콘", example = "family")
    private String icon;
    
    @OneToMany(mappedBy = "friendGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "그룹 멤버 목록")
    private List<FriendGroupMember> members = new ArrayList<>();
    
    @Schema(description = "그룹 생성 일시")
    private LocalDateTime createdAt;
    
    @Schema(description = "그룹 수정 일시")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 그룹에 친구 추가
     * 
     * @param friendship 추가할 친구 관계
     */
    public void addFriend(Friendship friendship) {
        FriendGroupMember member = new FriendGroupMember();
        member.setFriendGroup(this);
        member.setFriendship(friendship);
        member.setAddedAt(LocalDateTime.now());
        this.members.add(member);
    }
    
    /**
     * 그룹에서 친구 제거
     * 
     * @param friendship 제거할 친구 관계
     */
    public void removeFriend(Friendship friendship) {
        members.removeIf(member -> 
            member.getFriendship().getId().equals(friendship.getId()));
    }
    
    /**
     * 그룹 멤버 수 반환
     * 
     * @return 그룹 멤버 수
     */
    public int getMemberCount() {
        return members.size();
    }
}