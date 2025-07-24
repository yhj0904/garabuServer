package garabu.garabuServer.jwt;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.service.BlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final BlacklistService blacklistService;

    public JWTFilter(JWTUtil jwtUtil, BlacklistService blacklistService) {
        this.jwtUtil = jwtUtil;
        this.blacklistService = blacklistService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        
        // 인증이 필요없는 엔드포인트들은 JWT 검증을 건너뜀
        if (requestUri.equals("/reissue") || 
            requestUri.equals("/login") || 
            requestUri.equals("/join") ||
            requestUri.equals("/api/v2/join") ||
            requestUri.startsWith("/api/v2/mobile-oauth/") ||
            requestUri.startsWith("/swagger-ui/") ||
            requestUri.startsWith("/v3/api-docs/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String accessToken = null;
        
        // SSE 연결인 경우 쿼리 파라미터에서 토큰 추출
        if (requestUri.contains("/api/v2/sse/")) {
            accessToken = request.getParameter("token");
            if (accessToken == null) {
                // SSE는 헤더 제한이 있으므로 쿼리 파라미터로 토큰 전달
                String queryString = request.getQueryString();
                if (queryString != null && queryString.contains("token=")) {
                    String[] params = queryString.split("&");
                    for (String param : params) {
                        if (param.startsWith("token=")) {
                            accessToken = param.substring(6); // "token=" 제거
                            break;
                        }
                    }
                }
            }
        } else {
            // 일반 HTTP 요청인 경우 헤더에서 access키에 담긴 토큰을 꺼냄
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            accessToken = authHeader.substring(7); // "Bearer " 제거
        }

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        // 블랙리스트 체크
        String jti = jwtUtil.getJti(accessToken);
        if (blacklistService.isBlacklisted(jti)) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("blacklisted token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        if (requestUri.matches("^\\/login(?:\\/.*)?$")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (requestUri.matches("^\\/oauth2(?:\\/.*)?$")) {
            filterChain.doFilter(request, response);
            return;
        }

        // username, role 값을 획득
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        Member userEntity = new Member();
        userEntity.setUsername(username);
        userEntity.setRole(role);
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}