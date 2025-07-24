package garabu.garabuServer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 모바일 OAuth 로그인 요청 DTO
 * 
 * 프론트엔드 앱에서 받은 OAuth 토큰 정보를 담는 클래스
 */
@Data
public class MobileOAuthRequest {
    
    @NotBlank(message = "OAuth 제공자는 필수입니다")
    private String provider; // google, apple, naver, kakao
    
    @NotBlank(message = "Access Token은 필수입니다")
    private String accessToken;  // 일반적인 OAuth access token (Apple의 경우 identityToken)
    
    private String refreshToken; // 선택사항 (Apple의 경우 authorizationCode)
    
    private String idToken; // Google의 ID Token 또는 Apple의 Identity Token
    
    // 클라이언트에서 직접 전송하는 프로필 정보 (선택사항)
    private ProfileInfo profile;
    
    @Data
    public static class ProfileInfo {
        private String id;        // providerId
        private String email;
        private String name;
        private String nickname;
        private String profileImageUrl;
    }
}