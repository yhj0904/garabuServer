package garabu.garabuServer.controller;

import garabu.garabuServer.jwt.JWTConstants;
import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.service.BlacklistService;
import garabu.garabuServer.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT 토큰 재발급 컨트롤러
 * 
 * 토큰 로테이션, 재사용 감지, 유휴 만료 체크 기능 포함
 * 
 * @author yhj
 * @version 3.0
 */
@RestController
@Tag(name = "Auth", description = "인증 관리 API")
@Slf4j
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    public ReissueController(JWTUtil jwtUtil, RefreshTokenService refreshTokenService, BlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.blacklistService = blacklistService;
    }

    /**
     * JWT 토큰을 재발급합니다.
     * 
     * <p><strong>토큰 재발급 프로세스:</strong></p>
     * <ol>
     *   <li>Refresh Token 추출 (쿠키 또는 Authorization 헤더)</li>
     *   <li>토큰 유효성 검사 (만료, 형식, 카테고리)</li>
     *   <li>재사용 감지 - 이미 사용된 토큰인지 확인</li>
     *   <li>토큰 로테이션 - 기존 토큰 무효화</li>
     *   <li>새 Access Token + Refresh Token 생성</li>
     *   <li>블랙리스트에 기존 토큰 추가</li>
     *   <li>새 Refresh Token 저장</li>
     * </ol>
     * 
     * <p><strong>보안 기능:</strong></p>
     * <ul>
     *   <li>토큰 재사용 감지시 모든 토큰 무효화</li>
     *   <li>유휴 만료 (30일) 및 절대 만료 (60일) 체크</li>
     *   <li>사용자당 최대 5개 토큰 제한</li>
     * </ul>
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 토큰 재발급 결과
     */
    @PostMapping("/reissue")
    @Operation(
        summary = "JWT 토큰 재발급",
        description = """
                Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.
                
                **토큰 전송 방법:**
                1. HTTP 쿠키: `refresh=<token>`
                2. Authorization 헤더: `Bearer <token>`
                3. 요청 본문: `{"refreshToken": "<token>"}`
                
                **토큰 라이프사이클:**
                - Access Token: 10분 유효
                - Refresh Token: 60일 유효 (30일 유휴 시 만료)
                - 토큰 로테이션으로 Refresh Token 1회 사용 후 폐기
                
                **보안 기능:**
                - 재사용 감지시 모든 토큰 무효화
                - 블랙리스트를 통한 토큰 추적
                - 사용자별 토큰 수 제한
                """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 재발급 성공",
            content = @Content(
                schema = @Schema(implementation = TokenResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "성공 응답",
                    value = """
                            {
                              "message": "success",
                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                              "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
                            }
                            """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 Refresh Token 또는 만료된 토큰",
            content = @Content(
                schema = @Schema(implementation = TokenResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "오류 응답",
                    value = """
                            {
                              "message": "refresh token expired",
                              "accessToken": null,
                              "refreshToken": null
                            }
                            """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "보안 침해 감지 - 토큰 재사용",
            content = @Content(
                schema = @Schema(implementation = TokenResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "보안 침해 응답",
                    value = """
                            {
                              "message": "security breach - all tokens revoked",
                              "accessToken": null,
                              "refreshToken": null
                            }
                            """
                )
            )
        )
    })
    public ResponseEntity<?> reissue(
        @Parameter(description = "HTTP 요청 객체") HttpServletRequest request, 
        @Parameter(description = "HTTP 응답 객체") HttpServletResponse response) {

        // 1) refresh 토큰 추출 - 쿠키와 Authorization 헤더 모두 지원
        String refresh = extractRefreshToken(request);

        // 2) 방어 코드 : 토큰 없음
        if (refresh == null || refresh.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("refresh token null", null, null));
        }

        // 3) 토큰 만료 체크 (절대 만료 + 유휴 만료)
        try {
            if (jwtUtil.isExpiredWithIdle(refresh)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new TokenResponse("refresh token expired", null, null));
            }
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("refresh token expired", null, null));
        } catch (Exception e) {
            log.error("Token validation error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("invalid refresh token", null, null));
        }

        // 4) 토큰이 refresh인지 확인
        String category;
        try {
            category = jwtUtil.getCategory(refresh);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("invalid refresh token format", null, null));
        }

        if (!JWTConstants.REFRESH_TOKEN_CATEGORY.equals(category)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("invalid refresh token category", null, null));
        }

        // 5) 재사용 감지 - 이미 사용된 토큰인지 확인
        if (refreshTokenService.isTokenReused(refresh)) {
            // 보안 경고 - 탈취 가능성
            String username = jwtUtil.getUsername(refresh);
            log.warn("Token reuse detected for user: {}", username);
            
            // 해당 사용자의 모든 토큰 무효화
            handleSecurityBreach(username, "Token reuse detected");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenResponse("security breach - all tokens revoked", null, null));
        }

        // 6) Redis에 저장되어 있는지 확인
        Boolean isExist = refreshTokenService.existsByRefreshToken(refresh);
        if (!isExist) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TokenResponse("invalid refresh token", null, null));
        }

        // 7) 토큰 정보 추출
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        String oldJti = jwtUtil.getJti(refresh);

        // 8) 새로운 토큰 생성
        String newAccess = jwtUtil.createJwt(JWTConstants.ACCESS_TOKEN_CATEGORY, username, role, JWTConstants.ACCESS_TOKEN_EXPIRE_TIME);
        String newRefresh = jwtUtil.createJwt(JWTConstants.REFRESH_TOKEN_CATEGORY, username, role, JWTConstants.REFRESH_TOKEN_EXPIRE_TIME);
        
        // 새 토큰의 JTI 추출
        String newAccessJti = jwtUtil.getJti(newAccess);
        String newRefreshJti = jwtUtil.getJti(newRefresh);

        // 9) 토큰 로테이션 - 기존 토큰 삭제 및 블랙리스트 등록
        refreshTokenService.deleteRefreshToken(refresh);
        blacklistService.addToBlacklist(oldJti, "Token rotated", JWTConstants.BLACKLIST_TTL);

        // 10) 새 Refresh 토큰 저장
        boolean saved = refreshTokenService.saveRefreshToken(username, newRefresh, newRefreshJti, JWTConstants.REFRESH_TOKEN_EXPIRE_TIME);
        if (!saved) {
            log.error("Failed to save new refresh token for user: {}", username);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TokenResponse("token save failed", null, null));
        }

        // 11) 응답 설정
        response.setHeader("access", newAccess);
        response.setHeader("refresh", newRefresh);
        response.addCookie(createCookie("refresh", newRefresh));
        
        // CORS 헤더 설정
        response.setHeader("Access-Control-Expose-Headers", "access, refresh");

        log.info("Token reissued successfully for user: {}", username);
        return ResponseEntity.ok(new TokenResponse("success", newAccess, newRefresh));
    }

    /**
     * 요청에서 Refresh Token 추출
     */
    private String extractRefreshToken(HttpServletRequest request) {
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
        
        return refresh;
    }

    /**
     * 보안 침해 시 처리
     */
    private void handleSecurityBreach(String username, String reason) {
        try {
            // 1. 사용자의 모든 토큰 JTI 조회
            List<String> userJtis = refreshTokenService.getUserTokenJtis(username);
            
            // 2. 모든 토큰을 블랙리스트에 추가
            for (String jti : userJtis) {
                blacklistService.addToBlacklist(jti, reason, JWTConstants.BLACKLIST_TTL);
            }
            
            // 3. 사용자의 모든 리프레시 토큰 삭제
            refreshTokenService.deleteAllUserTokens(username);
            
            // 4. 보안 알림 (향후 구현 - 이메일, SMS 등)
            // notificationService.sendSecurityAlert(username, reason);
            
            log.warn("Security breach handled for user: {}, reason: {}", username, reason);
            
        } catch (Exception e) {
            log.error("Failed to handle security breach for user: {}", username, e);
        }
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
        cookie.setMaxAge(60*24*60*60); // 60일
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
    
    /**
     * 토큰 재발급 응답 DTO
     */
    @Schema(description = "토큰 재발급 응답 DTO")
    public static class TokenResponse {
        @Schema(description = "응답 메시지", example = "success")
        private String message;
        
        @Schema(description = "새로 발급된 Access Token", example = "eyJhbGciOiJIUzI1NiIs...")
        private String accessToken;
        
        @Schema(description = "새로 발급된 Refresh Token", example = "eyJhbGciOiJIUzI1NiIs...")
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
