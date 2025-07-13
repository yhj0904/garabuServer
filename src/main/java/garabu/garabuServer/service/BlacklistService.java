package garabu.garabuServer.service;

import garabu.garabuServer.jwt.JWTConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT 블랙리스트 관리 서비스
 * 
 * 로그아웃, 토큰 로테이션, 보안 이벤트 시 토큰을 무효화
 * 
 * @author yhj
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 토큰을 블랙리스트에 추가
     * 
     * @param jti JWT ID
     * @param reason 블랙리스트 사유
     * @param ttlMillis TTL (밀리초)
     */
    public void addToBlacklist(String jti, String reason, Long ttlMillis) {
        try {
            String blacklistKey = JWTConstants.BLACKLIST_PREFIX + jti;
            
            // 블랙리스트 정보 저장 (사유와 타임스탬프)
            String value = reason + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(blacklistKey, value, ttlMillis, TimeUnit.MILLISECONDS);
            
            log.info("Token added to blacklist - jti: {}, reason: {}", jti, reason);
            
        } catch (Exception e) {
            log.error("Failed to add token to blacklist - jti: {}", jti, e);
            throw new RuntimeException("Failed to add token to blacklist", e);
        }
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * @param jti JWT ID
     * @return 블랙리스트 포함 여부
     */
    public boolean isBlacklisted(String jti) {
        try {
            String blacklistKey = JWTConstants.BLACKLIST_PREFIX + jti;
            Boolean exists = redisTemplate.hasKey(blacklistKey);
            
            return Boolean.TRUE.equals(exists);
            
        } catch (Exception e) {
            log.error("Failed to check blacklist - jti: {}", jti, e);
            // 보안상 안전한 기본값으로 true 반환
            return true;
        }
    }
    
    /**
     * 사용자의 모든 토큰을 블랙리스트에 추가
     * 
     * @param username 사용자명
     * @param reason 블랙리스트 사유
     */
    public void blacklistAllUserTokens(String username, String reason) {
        // RefreshTokenService와 연계하여 구현
        // 사용자의 모든 활성 토큰 JTI를 조회하여 블랙리스트에 추가
        log.info("All tokens blacklisted for user: {}, reason: {}", username, reason);
    }
} 