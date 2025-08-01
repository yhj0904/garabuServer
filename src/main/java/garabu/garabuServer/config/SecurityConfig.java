package garabu.garabuServer.config;

import garabu.garabuServer.jwt.CustomLogoutFilter;
import garabu.garabuServer.jwt.JWTFilter;
import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.jwt.LoginFilter;
import garabu.garabuServer.oauth2.CustomSuccessHandler;
import garabu.garabuServer.service.CustomOAuth2UserService;
import garabu.garabuServer.service.RefreshTokenService;
import garabu.garabuServer.service.BlacklistService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthenticationConfiguration authenticationConfiguration;
    //JWTUtil 주입
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CustomSuccessHandler customSuccessHandler;
    private final BlacklistService blacklistService;
    
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:4000,http://localhost:8081}")
    private List<String> allowedOrigins;

    @Autowired
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                         CustomSuccessHandler customSuccessHandler,
                         JWTUtil jwtUtil,
                         RefreshTokenService refreshTokenService,
                         BlacklistService blacklistService,
                         AuthenticationConfiguration authenticationConfiguration) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.blacklistService = blacklistService;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public OncePerRequestFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    // Rate Limiting Filter 내부 클래스
    public static class RateLimitFilter extends OncePerRequestFilter {
        
        private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
        private static final int CAPACITY = 60; // 분당 60개 요청
        private static final Duration DURATION = Duration.ofMinutes(1);
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            String ip = getClientIP(request);
            Bucket bucket = cache.computeIfAbsent(ip, k -> createNewBucket());
            
            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(429); // Too Many Requests
                response.getWriter().write("Too many requests. Please try again later.");
                log.warn("Rate limit exceeded for IP: {}", ip);
            }
        }
        
        private Bucket createNewBucket() {
            Bandwidth limit = Bandwidth.classic(CAPACITY, Refill.intervally(CAPACITY, DURATION));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        }
        
        private String getClientIP(HttpServletRequest request) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null) {
                return xfHeader.split(",")[0];
            }
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null) {
                return xRealIP;
            }
            return request.getRemoteAddr();
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

      /*  http
                .authorizeHttpRequests((auth) -> auth
                        .anyRequest().permitAll());*/

        http
                .csrf((auth) -> auth.disable());

        http
                .formLogin((auth) -> auth.disable());

        http
                .httpBasic((auth) -> auth.disable());

        //oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );
        http
                .addFilterAfter(new JWTFilter(jwtUtil, blacklistService), OAuth2LoginAuthenticationFilter.class);

        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenService, blacklistService), LogoutFilter.class);

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/reissue",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/login", "/"
                                ,"/api/v2/join"
                                , "/join"
                                , "/api/v2/mobile-oauth/**"  // 모바일 OAuth 엔드포인트 허용
).permitAll()  // 더 이상 WebSocket을 사용하지 않음
                        .requestMatchers("/api/v2/sse/**").hasAnyRole("USER", "ADMIN")  // SSE는 인증 필요
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // ADMIN 권한 필요
                        .requestMatchers("/api/v2/**").hasAnyRole("USER", "ADMIN")  // USER 이상 권한 필요
                        .anyRequest().authenticated());

        //JWTFilter 등록
        http
                .addFilterBefore(new JWTFilter(jwtUtil, blacklistService), LoginFilter.class);

        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenService), UsernamePasswordAuthenticationFilter.class);

        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(allowedOrigins);
                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization", "access", "refresh"));

                        return configuration;
                    }
                }));

        // Rate Limit Filter 추가
        http
                .addFilterBefore(rateLimitFilter(), BasicAuthenticationFilter.class);


        return http.build();
    }
}