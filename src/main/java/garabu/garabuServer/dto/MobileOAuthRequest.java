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
    private String accessToken;
    
    private String refreshToken; // 선택사항
    
    private String idToken; // Apple의 경우 Identity Token
}