package garabu.garabuServer.repository;

import garabu.garabuServer.domain.RefreshEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Refresh Token Repository (DEPRECATED)
 * 
 * Redis 기반 RefreshTokenService로 대체됨
 * 성능 향상을 위해 RDB에서 Redis로 이전
 * 
 * @deprecated Redis 기반 토큰 관리로 대체됨
 */
@Deprecated
public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {

    Boolean existsByRefresh(String refresh);

    @Transactional
    void deleteByRefresh(String refresh);
}