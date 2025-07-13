package garabu.garabuServer.jwt;

/**
 * JWT 관련 상수 정의
 * 
 * @author yhj
 * @version 3.0
 */
public class JWTConstants {
    
    // 토큰 만료 시간
    public static final Long ACCESS_TOKEN_EXPIRE_TIME = 10 * 60 * 1000L; // 10분
    public static final Long REFRESH_TOKEN_EXPIRE_TIME = 60 * 24 * 60 * 60 * 1000L; // 60일
    public static final Long IDLE_EXPIRE_TIME = 30 * 24 * 60 * 60 * 1000L; // 30일 (유휴 만료)
    
    // Redis 키 접두사
    public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    public static final String USERNAME_TOKEN_PREFIX = "user_tokens:";
    public static final String BLACKLIST_PREFIX = "blacklist:";
    public static final String TOKEN_FAMILY_PREFIX = "token_family:";
    
    // 토큰 카테고리
    public static final String ACCESS_TOKEN_CATEGORY = "access";
    public static final String REFRESH_TOKEN_CATEGORY = "refresh";
    
    // 사용자당 최대 활성 토큰 수
    public static final int MAX_ACTIVE_TOKENS_PER_USER = 5;
    
    // 블랙리스트 TTL (access token 만료시간 + 버퍼)
    public static final Long BLACKLIST_TTL = ACCESS_TOKEN_EXPIRE_TIME + (5 * 60 * 1000L); // 15분
} 