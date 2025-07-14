package garabu.garabuServer.dto;

import garabu.garabuServer.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 카테고리 DTO 클래스
 * Redis 캐싱을 위한 직렬화 가능한 데이터 전송 객체
 * JPA 엔티티 대신 DTO를 캐싱하여 Hibernate 프록시 문제와 ClassCastException 방지
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리 DTO")
public class CategoryDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "카테고리 ID", example = "1")
    private Long id;
    
    @Schema(description = "카테고리명", example = "식비")
    private String category;
    
    @Schema(description = "카테고리 이모지", example = "🍽️")
    private String emoji;
    
    @Schema(description = "기본 카테고리 여부", example = "true")
    private Boolean isDefault;
    
    @Schema(description = "가계부 ID (사용자 정의 카테고리인 경우)", example = "1")
    private Long bookId;
    
    @Schema(description = "사용자 ID (사용자 정의 카테고리인 경우)", example = "1")
    private Long memberId;
    
    /**
     * Category 엔티티로부터 DTO를 생성하는 생성자
     * 
     * @param entity Category 엔티티
     */
    public CategoryDto(Category entity) {
        this.id = entity.getId();
        this.category = entity.getCategory();
        this.emoji = entity.getEmoji();
        this.isDefault = entity.getIsDefault();
        this.bookId = entity.getBook() != null ? entity.getBook().getId() : null;
        this.memberId = entity.getMember() != null ? entity.getMember().getId() : null;
    }
    
    /**
     * Category 엔티티로부터 DTO를 생성하는 정적 팩토리 메서드
     * 
     * @param entity Category 엔티티
     * @return CategoryDto 객체
     */
    public static CategoryDto from(Category entity) {
        return new CategoryDto(entity);
    }
}