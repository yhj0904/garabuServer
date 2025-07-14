package garabu.garabuServer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 초대 코드 생성 및 관리 서비스
 * 8자리 숫자 코드를 생성하고 Redis에 30분간 저장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteCodeService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();
    
    private static final String BOOK_INVITE_PREFIX = "BOOK_INVITE:";
    private static final String USER_ID_PREFIX = "USER_ID:";
    private static final long CODE_TTL_MINUTES = 30;
    
    /**
     * 가계부 초대 코드 생성
     * @param bookId 가계부 ID
     * @param role 부여할 권한
     * @return 8자리 초대 코드
     */
    public String generateBookInviteCode(Long bookId, String role) {
        String code = generateUniqueCode();
        String key = BOOK_INVITE_PREFIX + code;
        
        // Redis에 저장할 데이터
        BookInviteData inviteData = BookInviteData.builder()
                .bookId(bookId)
                .role(role)
                .build();
        
        redisTemplate.opsForValue().set(key, inviteData, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("가계부 초대 코드 생성: {} (bookId: {}, role: {})", code, bookId, role);
        
        return code;
    }
    
    /**
     * 사용자 식별 코드 생성
     * @param userId 사용자 ID
     * @return 8자리 식별 코드
     */
    public String generateUserIdCode(Long userId) {
        String code = generateUniqueCode();
        String key = USER_ID_PREFIX + code;
        
        redisTemplate.opsForValue().set(key, userId, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("사용자 식별 코드 생성: {} (userId: {})", code, userId);
        
        return code;
    }
    
    /**
     * 가계부 초대 코드 조회
     * @param code 초대 코드
     * @return 초대 정보 (없으면 null)
     */
    public BookInviteData getBookInviteData(String code) {
        String key = BOOK_INVITE_PREFIX + code;
        Object result = redisTemplate.opsForValue().get(key);
        
        if (result == null) {
            return null;
        }
        
        if (result instanceof BookInviteData) {
            return (BookInviteData) result;
        }
        
        if (result instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) result;
            return BookInviteData.builder()
                    .bookId(((Number) map.get("bookId")).longValue())
                    .role((String) map.get("role"))
                    .build();
        }
        
        throw new IllegalStateException("Unexpected Redis value type: " + result.getClass().getName());
    }
    
    /**
     * 사용자 ID 조회
     * @param code 식별 코드
     * @return 사용자 ID (없으면 null)
     */
    public Long getUserIdByCode(String code) {
        String key = USER_ID_PREFIX + code;
        return (Long) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 코드 만료 시간 조회 (초 단위)
     * @param code 코드
     * @param isBookInvite true: 가계부 초대 코드, false: 사용자 식별 코드
     * @return 남은 시간(초), 만료되었거나 없으면 -1
     */
    public long getCodeTTL(String code, boolean isBookInvite) {
        String key = (isBookInvite ? BOOK_INVITE_PREFIX : USER_ID_PREFIX) + code;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
    
    /**
     * 코드 삭제
     * @param code 코드
     * @param isBookInvite true: 가계부 초대 코드, false: 사용자 식별 코드
     */
    public void deleteCode(String code, boolean isBookInvite) {
        String key = (isBookInvite ? BOOK_INVITE_PREFIX : USER_ID_PREFIX) + code;
        redisTemplate.delete(key);
        log.info("코드 삭제: {}", key);
    }
    
    /**
     * 8자리 고유 숫자 코드 생성
     * @return 8자리 숫자 문자열
     */
    private String generateUniqueCode() {
        int code;
        String codeStr;
        int attempts = 0;
        
        do {
            // 10000000 ~ 99999999 범위의 8자리 숫자 생성
            code = 10000000 + secureRandom.nextInt(90000000);
            codeStr = String.valueOf(code);
            attempts++;
            
            // 무한 루프 방지
            if (attempts > 100) {
                throw new RuntimeException("고유 코드 생성 실패");
            }
        } while (isCodeExists(codeStr));
        
        return codeStr;
    }
    
    /**
     * 코드 중복 확인
     * @param code 확인할 코드
     * @return 중복 여부
     */
    private boolean isCodeExists(String code) {
        String bookKey = BOOK_INVITE_PREFIX + code;
        String userKey = USER_ID_PREFIX + code;
        
        return Boolean.TRUE.equals(redisTemplate.hasKey(bookKey)) || 
               Boolean.TRUE.equals(redisTemplate.hasKey(userKey));
    }
    
    /**
     * 가계부 초대 데이터 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BookInviteData {
        private Long bookId;
        private String role;
    }
} 