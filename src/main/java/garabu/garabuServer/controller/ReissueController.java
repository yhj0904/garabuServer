package garabu.garabuServer.controller;

import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * JWT 토큰 재발급 컨트롤러
 * 
 * Access Token과 Refresh Token의 재발급을 처리합니다.
 * Redis 기반 Refresh Token 관리로 성능 최적화
 * 
 * @author yhj
 * @version 2.0
 */
@RestController
@Tag(name = "Auth", description = "인증 관리 API")
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * ReissueController 생성자
     * 
     * @param jwtUtil JWT 유틸리티
     * @param refreshTokenService Redis 기반 Refresh 토큰 서비스
     */
    public ReissueController(JWTUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * JWT 토큰을 재발급합니다.
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 토큰 재발급 결과
     */
    @PostMapping("/reissue")
    @Operation(
        summary = "JWT 토큰 재발급",
        description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 Refresh Token 또는 만료된 토큰"
        )
    })
    public ResponseEntity<?> reissue(
        @Parameter(description = "HTTP 요청 객체") HttpServletRequest request, 
        @Parameter(description = "HTTP 응답 객체") HttpServletResponse response) {

        // 1) refresh 토큰 추출 - 쿠키와 Authorization 헤더 모두 지원
        String refresh = null;
        
        // 쿠키에서 추출 시도
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }
        
        // 쿠키에서 찾지 못한 경우 Authorization 헤더에서 추출
        if (refresh == null || refresh.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refresh = authHeader.substring(7);
            }
        }
        
        // 요청 본문에서 추출 시도 (JSON 형태)
        if (refresh == null || refresh.isBlank()) {
            try {
                String requestBody = request.getReader().lines()
                    .collect(java.util.stream.Collectors.joining(System.lineSeparator()));
                if (requestBody != null && !requestBody.isEmpty()) {
                    // 간단한 JSON 파싱 (refreshToken 필드 추출)
                    if (requestBody.contains("refreshToken")) {
                        String[] parts = requestBody.split("refreshToken");
                        if (parts.length > 1) {
                            String tokenPart = parts[1].replaceAll("[\"\s:{}]", "");
                            if (tokenPart.contains(",")) {
                                refresh = tokenPart.substring(0, tokenPart.indexOf(","));
                            } else {
                                refresh = tokenPart;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // JSON 파싱 실패시 무시
            }
        }

        // 2) 방어 코드 : 토큰 없음
        if (refresh == null || refresh.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("refresh token null", null, null));
        }


        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("refresh token expired", null, null));
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category;
        try {
            category = jwtUtil.getCategory(refresh);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("invalid refresh token format", null, null));
        }

        //Redis에 저장되어 있는지 확인
        Boolean isExist = refreshTokenService.existsByRefreshToken(refresh);
        if (!isExist) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("invalid refresh token", null, null));
        }

        if (!"refresh".equals(category)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("invalid refresh token category", null, null));
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //Refresh 토큰 저장 Redis에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshTokenService.deleteRefreshToken(refresh);
        refreshTokenService.saveRefreshToken(username, newRefresh, 86400000L);

        //response - 헤더와 쿠키 모두 설정, JSON 응답도 추가
        response.setHeader("access", newAccess);
        response.setHeader("refresh", newRefresh);
        response.addCookie(createCookie("refresh", newRefresh));
        
        // CORS 헤더 설정
        response.setHeader("Access-Control-Expose-Headers", "access, refresh");

        return ResponseEntity.ok(new TokenResponse("success", newAccess, newRefresh));
    }


    /**
     * HTTP 쿠키를 생성합니다.
     * 
     * @param key 쿠키 키
     * @param value 쿠키 값
     * @return 생성된 쿠키 객체
     */
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
    
    /**
     * 토큰 재발급 응답 DTO
     */
    public static class TokenResponse {
        private String message;
        private String accessToken;
        private String refreshToken;
        
        public TokenResponse(String message, String accessToken, String refreshToken) {
            this.message = message;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getAccessToken() {
            return accessToken;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
    }
}
