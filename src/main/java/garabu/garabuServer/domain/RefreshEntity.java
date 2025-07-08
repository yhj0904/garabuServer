package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Refresh 토큰 엔티티
 *
 * JWT Refresh 토큰 정보를 저장하는 JPA 엔티티입니다.
 */
@Entity
@Getter @Setter
@Schema(description = "Refresh 토큰 엔티티")
public class RefreshEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Refresh 엔티티 ID")
    private Long id;

    @Schema(description = "사용자명")
    private String username;
    @Schema(description = "Refresh 토큰 값")
    private String refresh;
    @Schema(description = "만료 일시")
    private String expiration;

}
