package garabu.garabuServer.domain;

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
@Getter @Setter
@Schema(description = "카테고리 엔티티")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @Schema(description = "카테고리 ID")
    private Long id;            // 카테고리 Id

    @Schema(description = "카테고리 이름", example = "급여")
    private String category;       // 카테고리 이름

    @Schema(description = "카테고리 이모지", example = "💰")
    private String emoji;          // 카테고리 이모지

    @Column(name = "is_default")
    @Schema(description = "기본 제공 카테고리 여부", example = "false")
    private Boolean isDefault = false;  // 기본 제공 카테고리 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @Schema(description = "소속 가계부 (사용자 정의 카테고리의 경우)")
    private Book book;            // 가계부 ID (사용자 정의 카테고리의 경우)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @Schema(description = "카테고리 생성자 (사용자 정의 카테고리의 경우)")
    private Member member;        // 카테고리 생성자 (사용자 정의 카테고리의 경우)
}
