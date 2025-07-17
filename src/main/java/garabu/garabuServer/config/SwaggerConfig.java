package garabu.garabuServer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger (OpenAPI 3.0) 설정 클래스
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Garabu API Server")
                        .version("v2.0")
                        .description("""
                                # Garabu (가라부) - 가계부 관리 API
                                
                                ## 개요
                                - 다중 사용자 가계부 관리 시스템
                                - 실시간 알림 및 협업 기능
                                - OAuth2 소셜 로그인 지원 (Google, Naver, Apple, Kakao)
                                - JWT 토큰 기반 인증
                                
                                ## 주요 기능
                                - 👥 **멀티 사용자 가계부**: 가족/친구와 함께 관리
                                - 💰 **수입/지출/이체 관리**: 상세한 금융 기록
                                - 📊 **카테고리별 통계**: 지출 패턴 분석
                                - 🔔 **실시간 알림**: 새로운 거래 내역 알림
                                - 🔐 **보안**: JWT 토큰 + OAuth2 인증
                                
                                ## 인증 방법
                                1. `/api/v2/mobile-oauth/login` 엔드포인트로 OAuth 로그인
                                2. 응답으로 받은 JWT 토큰을 `Authorization: Bearer <token>` 헤더에 추가
                                3. 토큰 만료 시 refresh token으로 갱신
                                
                                ## API 버전
                                - **v2**: 현재 안정 버전 (권장)
                                - 모든 엔드포인트는 `/api/v2/` 접두사 사용
                                """)
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Garabu Team")
                                .url("https://github.com/garabu-team")
                                .email("support@garabu.com"))
                        .license(new io.swagger.v3.oas.models.info.License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("oauth2"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .description("JWT 토큰을 입력하세요. 예: Bearer eyJhbGciOiJIUzI1NiIs..."))
                        .addSecuritySchemes("oauth2",
                                new SecurityScheme()
                                        .name("oauth2")
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("OAuth2 소셜 로그인 (Google, Naver, Apple, Kakao)")
                                        .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                                                .authorizationCode(new io.swagger.v3.oas.models.security.OAuthFlow()
                                                        .authorizationUrl("https://accounts.google.com/o/oauth2/v2/auth")
                                                        .tokenUrl("https://oauth2.googleapis.com/token")
                                                        .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                                .addString("openid", "OpenID Connect")
                                                                .addString("profile", "사용자 프로필 정보")
                                                                .addString("email", "이메일 주소"))))));
    }
}
// Swagger 접속 경로 : http://localhost:8080/swagger-ui/index.html