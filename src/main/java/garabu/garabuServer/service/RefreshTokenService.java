package garabu.garabuServer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 Refresh Token 관리 서비스
 * 
 * 기존 RDB 기반 저장 방식에서 Redis 기반으로 성능 최적화
 * - 자동 만료 기능 (TTL)
 * - 빠른 조회/삭제 성능
 * - 메모리 기반 저장으로 DB 부하 감소
 * 
 * @author yhj
 * @version 2.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USERNAME_TOKEN_PREFIX = "user_tokens:";
    
    /**
     * Refresh Token을 Redis에 저장
     * 
     * @param username 사용자명
     * @param refreshToken 리프레시 토큰
     * @param expiredMs 만료 시간 (밀리초)
     */
    public void saveRefreshToken(String username, String refreshToken, Long expiredMs) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            String userKey = USERNAME_TOKEN_PREFIX + username;
            
            // 1. 토큰 -> 사용자 매핑 저장 (토큰으로 사용자 조회용)
            redisTemplate.opsForValue().set(tokenKey, username, expiredMs, TimeUnit.MILLISECONDS);
            
            // 2. 사용자 -> 토큰 매핑 저장 (기존 토큰 삭제용)
            // 사용자별로 하나의 토큰만 유지
            String oldToken = (String) redisTemplate.opsForValue().getAndSet(userKey, refreshToken);
            redisTemplate.expire(userKey, Duration.ofMillis(expiredMs));
            
            // 3. 기존 토큰이 있다면 삭제
            if (oldToken != null && !oldToken.equals(refreshToken)) {
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + oldToken);
            }
            
            log.info("Refresh token saved for user: {}, TTL: {}ms", username, expiredMs);
            
        } catch (Exception e) {
            log.error("Failed to save refresh token for user: {}", username, e);
            throw new RuntimeException("Failed to save refresh token", e);
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
     * Refresh Token으로 사용자명 조회
     * 
     * @param refreshToken 리프레시 토큰
     * @return 사용자명 (토큰이 없거나 만료된 경우 null)
     */
    public String getUsernameByRefreshToken(String refreshToken) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            Object username = redisTemplate.opsForValue().get(tokenKey);
            
            return username != null ? username.toString() : null;
            
        } catch (Exception e) {
            log.error("Failed to get username by refresh token", e);
            return null;
        }
    }
    
    /**
     * Refresh Token 삭제
     * 
     * @param refreshToken 삭제할 리프레시 토큰
     */
    public void deleteRefreshToken(String refreshToken) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            
            // 1. 토큰으로 사용자명 조회
            String username = getUsernameByRefreshToken(refreshToken);
            
            // 2. 토큰 삭제
            redisTemplate.delete(tokenKey);
            
            // 3. 사용자-토큰 매핑에서도 삭제 (해당 토큰이 현재 토큰인 경우)
            if (username != null) {
                String userKey = USERNAME_TOKEN_PREFIX + username;
                String currentToken = (String) redisTemplate.opsForValue().get(userKey);
                if (refreshToken.equals(currentToken)) {
                    redisTemplate.delete(userKey);
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
    public void deleteAllRefreshTokensByUsername(String username) {
        try {
            String userKey = USERNAME_TOKEN_PREFIX + username;
            String currentToken = (String) redisTemplate.opsForValue().get(userKey);
            
            if (currentToken != null) {
                // 토큰 삭제
                redisTemplate.delete(REFRESH_TOKEN_PREFIX + currentToken);
                // 사용자-토큰 매핑 삭제
                redisTemplate.delete(userKey);
                
                log.info("All refresh tokens deleted for user: {}", username);
            }
            
        } catch (Exception e) {
            log.error("Failed to delete all refresh tokens for user: {}", username, e);
        }
    }
    
    /**
     * Refresh Token의 남은 TTL 조회
     * 
     * @param refreshToken 리프레시 토큰
     * @return TTL (초 단위, 만료되었거나 존재하지 않으면 -1)
     */
    public long getTokenTTL(String refreshToken) {
        try {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
            
            return ttl != null ? ttl : -1L;
            
        } catch (Exception e) {
            log.error("Failed to get token TTL: {}", refreshToken, e);
            return -1L;
        }
    }
}