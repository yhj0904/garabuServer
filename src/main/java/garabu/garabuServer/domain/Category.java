package garabu.garabuServer.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 카테고리 엔티티
 *
 * 가계부 기록의 분류(카테고리)를 관리하는 JPA 엔티티입니다.
 * 기본 제공 카테고리와 사용자 정의 카테고리를 모두 지원합니다.
 * 
 * - 기본 카테고리: isDefault=true, book=null, member=null
 * - 사용자 정의 카테고리: isDefault=false, book와 member 설정
 */
@Entity
@Table(name = "category",
       indexes = {
           @Index(name = "idx_category_is_default", columnList = "is_default"),
           @Index(name = "idx_category_book_id", columnList = "book_id"),
           @Index(name = "idx_category_member_id", columnList = "member_id")
       })
@Getter @Setter
@Schema(description = "카테고리 엔티티")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @Schema(description = "카테고리 ID")
    private Long id;

    @Schema(description = "카테고리 이름", example = "급여")
    private String category;

    @Schema(description = "카테고리 이모지", example = "💰")
    @Column(length = 10)
    private String emoji;

    @Column(name = "is_default")
    @Schema(description = "기본 제공 카테고리 여부", example = "false")
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_category_book"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "owner", "userBooks"})
    @Schema(description = "소속 가계부 (사용자 정의 카테고리의 경우)")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_category_member"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(description = "카테고리 생성자 (사용자 정의 카테고리의 경우)")
    private Member member;

    // 기본 생성자
    public Category() {}

    // 기본 카테고리 생성자
    public Category(String category, String emoji, Boolean isDefault) {
        this.category = category;
        this.emoji = emoji;
        this.isDefault = isDefault;
    }

    // 사용자 정의 카테고리 생성자
    public Category(String category, String emoji, Boolean isDefault, Book book, Member member) {
        this.category = category;
        this.emoji = emoji;
        this.isDefault = isDefault;
        this.book = book;
        this.member = member;
    }
}
