package garabu.garabuServer.controller;

import garabu.garabuServer.domain.RefreshEntity;
import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.repository.RefreshRepository;
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
 * Refresh Token을 사용하여 새로운 Access Token을 발급받을 수 있습니다.
 * 
 * @author yhj
 * @version 1.0
 */
@RestController
@Tag(name = "Auth", description = "인증 관리 API")
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    /**
     * ReissueController 생성자
     * 
     * @param jwtUtil JWT 유틸리티
     * @param refreshRepository Refresh 토큰 저장소
     */
    public ReissueController(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
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

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {

            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {

            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username, newRefresh, 86400000L);

        //response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Refresh 토큰 엔티티를 데이터베이스에 저장합니다.
     * 
     * @param username 사용자명
     * @param refresh Refresh 토큰
     * @param expiredMs 만료 시간 (밀리초)
     */
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
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
}
