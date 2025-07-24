package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 가계부 메모 엔티티
 * 
 * 가계부 전체에 대한 메모를 관리하는 JPA 엔티티입니다.
 * 한 가계부에는 하나의 메모만 존재할 수 있습니다.
 */
@Entity
@Table(name = "Memo")
@Getter @Setter
@Schema(description = "가계부 메모 엔티티")
public class Memo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "메모 ID")
    private Long id;
    
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    @Schema(description = "메모가 속한 가계부")
    private Book book;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @Schema(description = "메모 작성자 (최초 작성자)")
    private Member author;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "last_editor_id")
    @Schema(description = "마지막 수정자")
    private Member lastEditor;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    @Schema(description = "메모 내용")
    private String content;
    
    @Schema(description = "메모 작성 일시")
    private LocalDateTime createdAt;
    
    @Schema(description = "메모 수정 일시")
    private LocalDateTime updatedAt;
    
    @Schema(description = "메모 제목 (선택사항)")
    private String title;
    
    @Schema(description = "중요 표시 여부")
    private boolean important = false;
    
    @Schema(description = "메모 색상 (선택사항)")
    private String color;
    
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
     * 메모 업데이트
     * 
     * @param content 새로운 내용
     * @param editor 수정자
     */
    public void update(String content, Member editor) {
        this.content = content;
        this.lastEditor = editor;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 메모 업데이트 (제목 포함)
     * 
     * @param title 제목
     * @param content 내용
     * @param editor 수정자
     */
    public void update(String title, String content, Member editor) {
        this.title = title;
        this.content = content;
        this.lastEditor = editor;
        this.updatedAt = LocalDateTime.now();
    }
}