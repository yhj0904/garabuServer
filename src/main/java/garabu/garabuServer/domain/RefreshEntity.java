package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Refresh 토큰 엔티티 (DEPRECATED)
 *
 * Redis 기반 RefreshTokenService로 대체됨
 * 성능 향상을 위해 RDB에서 Redis로 이전
 * 
 * @deprecated Redis 기반 토큰 관리로 대체됨
 */
@Entity
@Getter @Setter
@Schema(description = "Refresh 토큰 엔티티")
@Deprecated
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
