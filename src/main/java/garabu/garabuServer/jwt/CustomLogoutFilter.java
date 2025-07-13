package garabu.garabuServer.jwt;

import garabu.garabuServer.service.BlacklistService;
import garabu.garabuServer.service.RefreshTokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final BlacklistService blacklistService;

    public CustomLogoutFilter(JWTUtil jwtUtil, RefreshTokenService refreshTokenService, BlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.blacklistService = blacklistService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        //path and method verify
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/logout$")) {

            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {

            filterChain.doFilter(request, response);
            return;
        }

        //get refresh token
        String refresh = null;
        
        // 1. 쿠키에서 추출 시도
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }
        
        // 2. 쿠키에서 찾지 못한 경우 Authorization 헤더에서 추출
        if (refresh == null || refresh.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                refresh = authHeader.substring(7);
            }
        }
        
        // 3. 요청 본문에서 추출 시도 (JSON 형태)
        if (refresh == null || refresh.isBlank()) {
            try {
                // Content-Type이 JSON인 경우에만 본문 파싱 시도
                String contentType = request.getContentType();
                if (contentType != null && contentType.contains("application/json")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> body = objectMapper.readValue(request.getInputStream(), Map.class);
                    if (body != null && body.containsKey("refreshToken")) {
                        refresh = body.get("refreshToken").toString();
                    }
                }
            } catch (Exception e) {
                // JSON 파싱 실패시 무시
            }
        }

        //refresh null check
        if (refresh == null || refresh.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            //response status code
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!JWTConstants.REFRESH_TOKEN_CATEGORY.equals(category)) {
            //response status code
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Redis에 저장되어 있는지 확인
        Boolean isExist = refreshTokenService.existsByRefreshToken(refresh);
        if (!isExist) {
            //response status code
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //로그아웃 진행
        try {
            // 1. Refresh Token의 JTI 추출
            String refreshJti = jwtUtil.getJti(refresh);
            String username = jwtUtil.getUsername(refresh);
            
            // 2. Refresh Token을 블랙리스트에 추가
            blacklistService.addToBlacklist(refreshJti, "User logout", JWTConstants.BLACKLIST_TTL);
            
            // 3. 사용자의 모든 토큰 무효화 (옵션 - 필요시 주석 해제)
            // refreshTokenService.deleteAllUserTokens(username);
            
            // 4. 현재 Refresh Token만 삭제
            refreshTokenService.deleteRefreshToken(refresh);
            
            log.info("User logout successful: {}", username);
            
        } catch (Exception e) {
            log.error("Error during logout process", e);
        }

        //Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}