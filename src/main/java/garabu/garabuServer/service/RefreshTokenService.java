package garabu.garabuServer.service;

import garabu.garabuServer.jwt.JWTConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 Refresh Token 관리 서비스
 * 
 * 토큰 로테이션, 재사용 감지, 사용자당 토큰 수 제한 기능 포함
 * 
 * @author yhj
 * @version 3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = JWTConstants.REFRESH_TOKEN_PREFIX;
    private static final String USERNAME_TOKEN_PREFIX = JWTConstants.USERNAME_TOKEN_PREFIX;
    private static final String TOKEN_FAMILY_PREFIX = JWTConstants.TOKEN_FAMILY_PREFIX;
    
    /**
     * Refresh Token을 Redis에 저장 (토큰 로테이션 지원)
     * 
     * @param username 사용자명
     * @param refreshToken 리프레시 토큰
     * @param jti JWT ID
     * @param expiredMs 만료 시간 (밀리초)
     * @return 저장 성공 여부
     */
    public boolean saveRefreshToken(String username, String refreshToken, String jti, Long expiredMs) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            String userKey = USERNAME_TOKEN_PREFIX + username;
            
            // 토큰 정보 객체 생성
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("username", username);
            tokenInfo.put("jti", jti);
            tokenInfo.put("issuedAt", System.currentTimeMillis());
            tokenInfo.put("lastUsed", System.currentTimeMillis());
            
            // 1. 토큰 -> 사용자 정보 매핑 저장
            redisTemplate.opsForValue().set(tokenKey, tokenInfo, expiredMs, TimeUnit.MILLISECONDS);
            
            // 2. 사용자의 활성 토큰 목록 관리 (최대 5개)
            manageUserTokens(username, refreshToken, expiredMs);
            
            log.info("Refresh token saved for user: {}, jti: {}, TTL: {}ms", username, jti, expiredMs);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to save refresh token for user: {}", username, e);
            return false;
        }
    }
    
    /**
     * 사용자별 토큰 수 제한 관리
     */
    private void manageUserTokens(String username, String newToken, Long expiredMs) {
        String userKey = USERNAME_TOKEN_PREFIX + username;
        
        // Lua 스크립트로 원자성 보장
        String luaScript = """
            local userKey = KEYS[1]
            local newToken = ARGV[1]
            local maxTokens = tonumber(ARGV[2])
            local ttl = tonumber(ARGV[3])
            
            -- 입력값 검증
            if not maxTokens then
                maxTokens = 5  -- 기본값
            end
            if not ttl or ttl <= 0 then
                ttl = 86400  -- 기본값 1일
            end
            
            redis.call('lpush', userKey, newToken)
            local count = redis.call('llen', userKey)
            
            if count > maxTokens then
                local removedToken = redis.call('rpop', userKey)
                if removedToken then
                    redis.call('del', 'refresh_token:' .. removedToken)
                end
            end
            
            redis.call('expire', userKey, ttl)
            return count
        """;
        
        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        
        // TTL 계산 안전하게 처리
        long ttlSeconds = Math.min(expiredMs / 1000, Integer.MAX_VALUE);
        
        Long count = redisTemplate.execute(script, 
            Collections.singletonList(userKey),
            newToken, 
            String.valueOf(JWTConstants.MAX_ACTIVE_TOKENS_PER_USER),
            String.valueOf(ttlSeconds));
            
        log.debug("User {} has {} active tokens", username, count);
    }
    
    /**
     * Refresh Token 재사용 감지
     * 
     * @param refreshToken 검증할 리프레시 토큰
     * @return 재사용 여부 (true: 재사용 감지됨, false: 정상)
     */
    public boolean isTokenReused(String refreshToken) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            Object tokenInfo = redisTemplate.opsForValue().get(tokenKey);
            
            if (tokenInfo == null) {
                // 토큰이 존재하지 않음 - 이미 사용되었거나 만료됨
                log.warn("Token reuse detected - token not found: {}", refreshToken.substring(0, 20) + "...");
                return true;
            }
            
            // 토큰이 존재하면 정상
            return false;
            
        } catch (Exception e) {
            log.error("Failed to check token reuse", e);
            return true; // 안전한 기본값
        }
    }
    
    /**
     * Refresh Token 존재 여부 확인
     * 
     * @param refreshToken 리프레시 토큰
     * @return 존재 여부
     */
    public boolean existsByRefreshToken(String refreshToken) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            Boolean exists = redisTemplate.hasKey(tokenKey);
            
            log.debug("Refresh token exists check: {}, result: {}", refreshToken.substring(0, 10) + "...", exists);
            return Boolean.TRUE.equals(exists);
            
        } catch (Exception e) {
            log.error("Failed to check refresh token existence: {}", refreshToken, e);
            return false;
        }
    }
    
    /**
     * Refresh Token으로 토큰 정보 조회
     * 
     * @param refreshToken 리프레시 토큰
     * @return 토큰 정보 맵 (없으면 null)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTokenInfo(String refreshToken) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            Object tokenInfo = redisTemplate.opsForValue().get(tokenKey);
            
            if (tokenInfo instanceof Map) {
                return (Map<String, Object>) tokenInfo;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to get token info", e);
            return null;
        }
    }
    
    /**
     * Refresh Token으로 사용자명 조회
     * 
     * @param refreshToken 리프레시 토큰
     * @return 사용자명 (토큰이 없거나 만료된 경우 null)
     */
    public String getUsernameByRefreshToken(String refreshToken) {
        Map<String, Object> tokenInfo = getTokenInfo(refreshToken);
        return tokenInfo != null ? (String) tokenInfo.get("username") : null;
    }
    
    /**
     * Refresh Token 삭제
     * 
     * @param refreshToken 삭제할 리프레시 토큰
     */
    public void deleteRefreshToken(String refreshToken) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            
            // 1. 토큰 정보 조회
            Map<String, Object> tokenInfo = getTokenInfo(refreshToken);
            
            // 2. 토큰 삭제
            redisTemplate.delete(tokenKey);
            
            // 3. 사용자-토큰 매핑에서도 삭제
            if (tokenInfo != null) {
                String username = (String) tokenInfo.get("username");
                if (username != null) {
                    String userKey = USERNAME_TOKEN_PREFIX + username;
                    redisTemplate.opsForList().remove(userKey, 1, refreshToken);
                }
            }
            
            log.info("Refresh token deleted: {}", refreshToken.substring(0, 10) + "...");
            
        } catch (Exception e) {
            log.error("Failed to delete refresh token: {}", refreshToken, e);
        }
    }
    
    /**
     * 사용자의 모든 Refresh Token 삭제 (로그아웃 시 사용)
     * 
     * @param username 사용자명
     */
    public void deleteAllUserTokens(String username) {
        try {
            String userKey = USERNAME_TOKEN_PREFIX + username;
            
            // 사용자의 모든 토큰 조회
            List<Object> userTokens = redisTemplate.opsForList().range(userKey, 0, -1);
            
            if (userTokens != null && !userTokens.isEmpty()) {
                // 각 토큰 삭제
                for (Object token : userTokens) {
                    String tokenKey = REFRESH_TOKEN_PREFIX + token;
                    redisTemplate.delete(tokenKey);
                }
                
                // 사용자 토큰 목록 삭제
                redisTemplate.delete(userKey);
            }
            
            log.info("All refresh tokens deleted for user: {}", username);
            
        } catch (Exception e) {
            log.error("Failed to delete all tokens for user: {}", username, e);
        }
    }
    
    /**
     * 사용자의 활성 토큰 JTI 목록 조회 (블랙리스트 처리용)
     * 
     * @param username 사용자명
     * @return JTI 목록
     */
    public List<String> getUserTokenJtis(String username) {
        List<String> jtis = new ArrayList<>();
        
        try {
            String userKey = USERNAME_TOKEN_PREFIX + username;
            List<Object> userTokens = redisTemplate.opsForList().range(userKey, 0, -1);
            
            if (userTokens != null) {
                for (Object token : userTokens) {
                    Map<String, Object> tokenInfo = getTokenInfo(token.toString());
                    if (tokenInfo != null && tokenInfo.containsKey("jti")) {
                        jtis.add((String) tokenInfo.get("jti"));
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to get user token JTIs", e);
        }
        
        return jtis;
    }
    
    /**
     * 기존 메서드 호환성 유지
     */
    public void saveRefreshToken(String username, String refreshToken, Long expiredMs) {
        // JTI는 토큰에서 추출해야 하지만, 기존 호환성을 위해 임시 처리
        saveRefreshToken(username, refreshToken, UUID.randomUUID().toString(), expiredMs);
    }
}