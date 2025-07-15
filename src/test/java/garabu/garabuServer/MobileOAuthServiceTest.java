package garabu.garabuServer;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.MobileOAuthRequest;
import garabu.garabuServer.dto.MobileOAuthResponse;
import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.service.MobileOAuthService;
import garabu.garabuServer.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileOAuthServiceTest {

    @Mock
    private MemberJPARepository memberJPARepository;
    
    @Mock
    private JWTUtil jwtUtil;
    
    @Mock
    private RefreshTokenService refreshTokenService;
    
    @InjectMocks
    private MobileOAuthService mobileOAuthService;
    
    private MobileOAuthRequest request;
    private Member testMember;
    
    @BeforeEach
    void setUp() {
        request = new MobileOAuthRequest();
        request.setProvider("google");
        request.setAccessToken("test_access_token");
        
        testMember = new Member();
        testMember.setId(1L);
        testMember.setUsername("google test_user_id");
        testMember.setEmail("test@example.com");
        testMember.setName("Test User");
        testMember.setProviderId("test_user_id");
        testMember.setRole("ROLE_USER");
        
        // Google Client ID 설정
        ReflectionTestUtils.setField(mobileOAuthService, "googleClientId", "test_client_id");
    }
    
    @Test
    void 신규_사용자_OAuth_로그인_성공() {
        // Given
        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(memberJPARepository.save(any(Member.class)))
                .thenReturn(testMember);
        when(jwtUtil.createJwt(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn("test_jwt_token");
        when(jwtUtil.getJti(anyString()))
                .thenReturn("test_jti");
        
        // When
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getUser());
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        
        verify(memberJPARepository, times(1)).save(any(Member.class));
        verify(refreshTokenService, times(1)).saveRefreshToken(anyString(), anyString(), anyString(), anyLong());
    }
    
    @Test
    void 기존_사용자_OAuth_로그인_성공() {
        // Given
        when(memberJPARepository.findByProviderIdAndEmail(anyString(), anyString()))
                .thenReturn(Optional.of(testMember));
        when(memberJPARepository.save(any(Member.class)))
                .thenReturn(testMember);
        when(jwtUtil.createJwt(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn("test_jwt_token");
        when(jwtUtil.getJti(anyString()))
                .thenReturn("test_jti");
        
        // When
        MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        
        verify(memberJPARepository, times(1)).save(any(Member.class));
        verify(refreshTokenService, times(1)).saveRefreshToken(anyString(), anyString(), anyString(), anyLong());
    }
    
    @Test
    void 지원하지_않는_프로바이더_예외() {
        // Given
        request.setProvider("unsupported");
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            mobileOAuthService.processOAuthLogin(request);
        });
    }
}