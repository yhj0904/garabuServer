package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 카테고리 엔티티
 *
 * 가계부 기록의 분류(카테고리)를 관리하는 JPA 엔티티입니다.
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

}
