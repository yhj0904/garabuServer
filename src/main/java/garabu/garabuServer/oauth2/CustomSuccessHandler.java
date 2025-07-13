package garabu.garabuServer.oauth2;

import garabu.garabuServer.dto.CustomOAuth2User;
import garabu.garabuServer.jwt.JWTConstants;
import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성 (jti 포함)
        String access = jwtUtil.createJwt(JWTConstants.ACCESS_TOKEN_CATEGORY, username, role, JWTConstants.ACCESS_TOKEN_EXPIRE_TIME);
        String refresh = jwtUtil.createJwt(JWTConstants.REFRESH_TOKEN_CATEGORY, username, role, JWTConstants.REFRESH_TOKEN_EXPIRE_TIME);
        
        // JTI 추출
        String refreshJti = jwtUtil.getJti(refresh);

        //Refresh 토큰 저장 (Redis)
        refreshTokenService.saveRefreshToken(username, refresh, refreshJti, JWTConstants.REFRESH_TOKEN_EXPIRE_TIME);

        //응답 설정
        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
        response.sendRedirect("http://localhost:4000/OAuth");

    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*24*60*60); // 60일
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}