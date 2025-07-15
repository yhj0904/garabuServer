package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.MobileOAuthRequest;
import garabu.garabuServer.dto.MobileOAuthResponse;
import garabu.garabuServer.jwt.JWTConstants;
import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.repository.MemberJPARepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MobileOAuthService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class MobileOAuthServiceTest {

    @Mock
    private MemberJPARepository memberJPARepository;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AppleOAuthService appleOAuthService;

    @InjectMocks
    private MobileOAuthService mobileOAuthService;

    private Member testMember;
    private MobileOAuthRequest request;

    @BeforeEach
    void setUp() {
        // 테스트 멤버 설정
        testMember = new Member();
        testMember.setId(1L);
        testMember.setUsername("google 12345");
        testMember.setEmail("test@gmail.com");
        testMember.setName("Test User");
        testMember.setRole("ROLE_USER");
        testMember.setProviderId("12345");

        // Google client ID 설정
        ReflectionTestUtils.setField(mobileOAuthService, "googleClientId", "test-google-client-id");
    }

    @Test
    @DisplayName("Google OAuth 로그인 - 새 사용자")
    void processOAuthLogin_Google_NewUser() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("google");
        request.setAccessToken("google_access_token");

        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(memberJPARepository.save(any(Member.class))).thenReturn(testMember);
        when(jwtUtil.createJwt(eq(JWTConstants.ACCESS_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("access_token");
        when(jwtUtil.createJwt(eq(JWTConstants.REFRESH_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("refresh_token");
        when(jwtUtil.getJti(anyString())).thenReturn("jti-123");
        when(refreshTokenService.saveRefreshToken(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(true);

        // when
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@gmail.com");

        verify(memberJPARepository).save(any(Member.class));
        verify(refreshTokenService).saveRefreshToken(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Google OAuth 로그인 - 기존 사용자")
    void processOAuthLogin_Google_ExistingUser() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("google");
        request.setAccessToken("google_access_token");

        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testMember));
        when(memberJPARepository.save(any(Member.class))).thenReturn(testMember);
        when(jwtUtil.createJwt(eq(JWTConstants.ACCESS_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("access_token");
        when(jwtUtil.createJwt(eq(JWTConstants.REFRESH_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("refresh_token");
        when(jwtUtil.getJti(anyString())).thenReturn("jti-123");
        when(refreshTokenService.saveRefreshToken(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(true);

        // when
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@gmail.com");
        
        verify(memberJPARepository).save(testMember); // 기존 사용자 정보 업데이트
    }

    @Test
    @DisplayName("Apple OAuth 로그인 - Identity Token 사용")
    void processOAuthLogin_Apple_WithIdentityToken() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("apple");
        request.setIdToken("apple_identity_token");

        Map<String, Object> appleUserInfo = Map.of(
            "sub", "apple_user_id",
            "email", "test@icloud.com",
            "email_verified", true,
            "is_private_email", false
        );

        Member appleMember = new Member();
        appleMember.setId(2L);
        appleMember.setUsername("apple apple_user_id");
        appleMember.setEmail("test@icloud.com");
        appleMember.setName("test");
        appleMember.setRole("ROLE_USER");
        appleMember.setProviderId("apple_user_id");

        when(appleOAuthService.extractUserInfoFromIdentityToken("apple_identity_token"))
            .thenReturn(appleUserInfo);
        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(memberJPARepository.save(any(Member.class))).thenReturn(appleMember);
        when(jwtUtil.createJwt(eq(JWTConstants.ACCESS_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("apple_access_token");
        when(jwtUtil.createJwt(eq(JWTConstants.REFRESH_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("apple_refresh_token");
        when(jwtUtil.getJti(anyString())).thenReturn("apple-jti-123");
        when(refreshTokenService.saveRefreshToken(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(true);

        // when
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@icloud.com");
        assertThat(response.getToken()).isEqualTo("apple_access_token");
        
        verify(appleOAuthService).extractUserInfoFromIdentityToken("apple_identity_token");
    }

    @Test
    @DisplayName("지원하지 않는 OAuth 제공자")
    void processOAuthLogin_UnsupportedProvider() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("unsupported");
        request.setAccessToken("some_token");

        // when & then
        assertThatThrownBy(() -> mobileOAuthService.processOAuthLogin(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("OAuth 로그인 처리 실패");
    }

    @Test
    @DisplayName("OAuth 토큰 검증 실패")
    void processOAuthLogin_InvalidToken() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("google");
        request.setAccessToken("invalid_token");

        // RestTemplate이 예외를 던지도록 설정
        // 실제 구현에서는 RestTemplate을 Mock으로 주입해야 함

        // when & then
        assertThatThrownBy(() -> mobileOAuthService.processOAuthLogin(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("OAuth 로그인 처리 실패");
    }

    @Test
    @DisplayName("Refresh Token 저장 실패")
    void processOAuthLogin_RefreshTokenSaveFailed() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("google");
        request.setAccessToken("google_access_token");

        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testMember));
        when(memberJPARepository.save(any(Member.class))).thenReturn(testMember);
        when(jwtUtil.createJwt(eq(JWTConstants.ACCESS_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("access_token");
        when(jwtUtil.createJwt(eq(JWTConstants.REFRESH_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("refresh_token");
        when(jwtUtil.getJti(anyString())).thenReturn("jti-123");
        when(refreshTokenService.saveRefreshToken(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(false); // 저장 실패

        // when
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);

        // then
        // 저장 실패해도 응답은 정상적으로 반환 (로그만 남김)
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access_token");
    }

    @Test
    @DisplayName("Naver OAuth 로그인 성공")
    void processOAuthLogin_Naver_Success() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("naver");
        request.setAccessToken("naver_access_token");

        Member naverMember = new Member();
        naverMember.setId(3L);
        naverMember.setUsername("naver naver_user_id");
        naverMember.setEmail("test@naver.com");
        naverMember.setName("네이버 사용자");
        naverMember.setRole("ROLE_USER");
        naverMember.setProviderId("naver_user_id");

        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(memberJPARepository.save(any(Member.class))).thenReturn(naverMember);
        when(jwtUtil.createJwt(eq(JWTConstants.ACCESS_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("naver_access_token");
        when(jwtUtil.createJwt(eq(JWTConstants.REFRESH_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("naver_refresh_token");
        when(jwtUtil.getJti(anyString())).thenReturn("naver-jti-123");
        when(refreshTokenService.saveRefreshToken(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(true);

        // when
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@naver.com");
        assertThat(response.getUser().getName()).isEqualTo("네이버 사용자");
    }

    @Test
    @DisplayName("Kakao OAuth 로그인 성공")
    void processOAuthLogin_Kakao_Success() {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("kakao");
        request.setAccessToken("kakao_access_token");

        Member kakaoMember = new Member();
        kakaoMember.setId(4L);
        kakaoMember.setUsername("kakao kakao_user_id");
        kakaoMember.setEmail("test@kakao.com");
        kakaoMember.setName("카카오 사용자");
        kakaoMember.setRole("ROLE_USER");
        kakaoMember.setProviderId("kakao_user_id");

        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(memberJPARepository.save(any(Member.class))).thenReturn(kakaoMember);
        when(jwtUtil.createJwt(eq(JWTConstants.ACCESS_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("kakao_access_token");
        when(jwtUtil.createJwt(eq(JWTConstants.REFRESH_TOKEN_CATEGORY), anyString(), anyString(), anyLong()))
            .thenReturn("kakao_refresh_token");
        when(jwtUtil.getJti(anyString())).thenReturn("kakao-jti-123");
        when(refreshTokenService.saveRefreshToken(anyString(), anyString(), anyString(), anyLong()))
            .thenReturn(true);

        // when
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@kakao.com");
        assertThat(response.getUser().getName()).isEqualTo("카카오 사용자");
    }
}