package garabu.garabuServer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Set;
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
    private static final String FRIEND_INVITE_PREFIX = "FRIEND_INVITE:";
    private static final long CODE_TTL_MINUTES = 30;
    
    /**
     * 가계부 초대 코드 생성 또는 기존 코드 반환
     * @param bookId 가계부 ID
     * @param role 부여할 권한
     * @return 8자리 초대 코드
     */
    public String generateBookInviteCode(Long bookId, String role) {
        // 기존에 유효한 코드가 있는지 확인
        String existingCode = findExistingInviteCode(bookId, role);
        if (existingCode != null) {
            log.info("기존 가계부 초대 코드 재사용: {} (bookId: {}, role: {})", existingCode, bookId, role);
            return existingCode;
        }
        
        // 새 코드 생성
        String code = generateUniqueCode();
        String key = BOOK_INVITE_PREFIX + code;
        
        // Redis에 저장할 데이터
        BookInviteData inviteData = BookInviteData.builder()
                .bookId(bookId)
                .role(role)
                .build();
        
        redisTemplate.opsForValue().set(key, inviteData, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("새 가계부 초대 코드 생성: {} (bookId: {}, role: {})", code, bookId, role);
        
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
     * 친구 초대 코드 생성
     * @param userId 사용자 ID
     * @return 8자리 친구 초대 코드
     */
    public String generateFriendInviteCode(Long userId) {
        // 기존에 유효한 친구 초대 코드가 있는지 확인
        String existingCode = findExistingFriendInviteCode(userId);
        if (existingCode != null) {
            log.info("기존 친구 초대 코드 재사용: {} (userId: {})", existingCode, userId);
            return existingCode;
        }
        
        String code = generateUniqueCode();
        String key = FRIEND_INVITE_PREFIX + code;
        
        redisTemplate.opsForValue().set(key, userId, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("새 친구 초대 코드 생성: {} (userId: {})", code, userId);
        
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
     * 친구 초대 코드로 사용자 ID 조회
     * @param code 친구 초대 코드
     * @return 사용자 ID (없으면 null)
     */
    public Long getUserIdByFriendInviteCode(String code) {
        String key = FRIEND_INVITE_PREFIX + code;
        return (Long) redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 코드 만료 시간 조회 (초 단위)
     * @param code 코드
     * @param codeType 코드 타입: "BOOK", "USER", "FRIEND"
     * @return 남은 시간(초), 만료되었거나 없으면 -1
     */
    public long getCodeTTL(String code, String codeType) {
        String prefix;
        switch (codeType.toUpperCase()) {
            case "BOOK":
                prefix = BOOK_INVITE_PREFIX;
                break;
            case "USER":
                prefix = USER_ID_PREFIX;
                break;
            case "FRIEND":
                prefix = FRIEND_INVITE_PREFIX;
                break;
            default:
                throw new IllegalArgumentException("Invalid code type: " + codeType);
        }
        
        String key = prefix + code;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }
    
    /**
     * 코드 삭제
     * @param code 코드
     * @param codeType 코드 타입: "BOOK", "USER", "FRIEND"
     */
    public void deleteCode(String code, String codeType) {
        String prefix;
        switch (codeType.toUpperCase()) {
            case "BOOK":
                prefix = BOOK_INVITE_PREFIX;
                break;
            case "USER":
                prefix = USER_ID_PREFIX;
                break;
            case "FRIEND":
                prefix = FRIEND_INVITE_PREFIX;
                break;
            default:
                throw new IllegalArgumentException("Invalid code type: " + codeType);
        }
        
        String key = prefix + code;
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
        String friendKey = FRIEND_INVITE_PREFIX + code;
        
        return Boolean.TRUE.equals(redisTemplate.hasKey(bookKey)) || 
               Boolean.TRUE.equals(redisTemplate.hasKey(userKey)) ||
               Boolean.TRUE.equals(redisTemplate.hasKey(friendKey));
    }
    
    /**
     * 기존 유효한 초대 코드 찾기
     * @param bookId 가계부 ID
     * @param role 권한
     * @return 유효한 코드 또는 null
     */
    private String findExistingInviteCode(Long bookId, String role) {
        // Redis에서 BOOK_INVITE:* 패턴의 모든 키 조회
        Set<String> keys = redisTemplate.keys(BOOK_INVITE_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        
        for (String key : keys) {
            BookInviteData data = getBookInviteDataByKey(key);
            if (data != null && 
                data.getBookId().equals(bookId) && 
                data.getRole().equals(role)) {
                // TTL이 5분 이상 남은 경우에만 재사용
                Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
                if (ttl != null && ttl > 5) {
                    String code = key.substring(BOOK_INVITE_PREFIX.length());
                    return code;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Redis 키로 BookInviteData 조회
     * @param key Redis 키
     * @return BookInviteData 또는 null
     */
    private BookInviteData getBookInviteDataByKey(String key) {
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
        
        return null;
    }
    
    /**
     * 기존 유효한 친구 초대 코드 찾기
     * @param userId 사용자 ID
     * @return 유효한 코드 또는 null
     */
    private String findExistingFriendInviteCode(Long userId) {
        // Redis에서 FRIEND_INVITE:* 패턴의 모든 키 조회
        Set<String> keys = redisTemplate.keys(FRIEND_INVITE_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        
        for (String key : keys) {
            Object storedUserIdObj = redisTemplate.opsForValue().get(key);
            Long storedUserId = null;
            if (storedUserIdObj instanceof Integer) {
                storedUserId = ((Integer) storedUserIdObj).longValue();
            } else if (storedUserIdObj instanceof Long) {
                storedUserId = (Long) storedUserIdObj;
            }
            if (storedUserId != null && storedUserId.equals(userId)) {
                // TTL이 5분 이상 남은 경우에만 재사용
                Long ttl = redisTemplate.getExpire(key, TimeUnit.MINUTES);
                if (ttl != null && ttl > 5) {
                    String code = key.substring(FRIEND_INVITE_PREFIX.length());
                    return code;
                }
            }
        }
        
        return null;
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