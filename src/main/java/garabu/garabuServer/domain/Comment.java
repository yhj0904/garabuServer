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
 * 댓글 엔티티
 * 
 * 가계부 및 가계부 내역에 대한 댓글을 관리하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "댓글 엔티티")
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "댓글 ID")
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @Schema(description = "댓글 작성자")
    private Member author;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @Schema(description = "댓글이 속한 가계부")
    private Book book;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ledger_id")
    @Schema(description = "댓글이 속한 가계부 내역 (가계부 내역 댓글인 경우)")
    private Ledger ledger;
    
    @Enumerated(EnumType.STRING)
    @Schema(description = "댓글 타입", example = "BOOK")
    private CommentType commentType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @Schema(description = "댓글 내용")
    private String content;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    @Schema(description = "부모 댓글 (대댓글인 경우)")
    private Comment parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "대댓글 목록")
    private List<Comment> replies = new ArrayList<>();
    
    @Schema(description = "댓글 작성 일시")
    private LocalDateTime createdAt;
    
    @Schema(description = "댓글 수정 일시")
    private LocalDateTime updatedAt;
    
    @Schema(description = "댓글 삭제 일시")
    private LocalDateTime deletedAt;
    
    @Schema(description = "댓글 좋아요 수")
    private int likeCount = 0;
    
    @Schema(description = "댓글 고정 여부")
    private boolean pinned = false;
    
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
     * 댓글 삭제 처리
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * 댓글 삭제 여부 확인
     * 
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    /**
     * 대댓글 추가
     * 
     * @param reply 추가할 대댓글
     */
    public void addReply(Comment reply) {
        reply.setParent(this);
        this.replies.add(reply);
    }
    
    /**
     * 대댓글 제거
     * 
     * @param reply 제거할 대댓글
     */
    public void removeReply(Comment reply) {
        reply.setParent(null);
        this.replies.remove(reply);
    }
    
    /**
     * 최상위 댓글인지 확인
     * 
     * @return 최상위 댓글 여부
     */
    public boolean isTopLevel() {
        return parent == null;
    }
    
    /**
     * 댓글 깊이 반환
     * 
     * @return 댓글 깊이 (0: 최상위, 1: 대댓글, ...)
     */
    public int getDepth() {
        if (parent == null) {
            return 0;
        }
        return parent.getDepth() + 1;
    }
}