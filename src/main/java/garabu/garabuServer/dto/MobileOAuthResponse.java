package garabu.garabuServer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모바일 OAuth 로그인 응답 DTO
 * 
 * 모바일 앱에게 전달할 OAuth 로그인 결과 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileOAuthResponse {
    private UserDTO user;
    private String token;        // JWT Access Token
    private String refreshToken; // JWT Refresh Token
}