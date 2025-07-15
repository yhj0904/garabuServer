package garabu.garabuServer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import garabu.garabuServer.dto.*;
import garabu.garabuServer.service.MobileOAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MobileOAuthController 단위 테스트
 */
@WebMvcTest(MobileOAuthController.class)
class MobileOAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MobileOAuthService mobileOAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    private MobileOAuthRequest request;
    private MobileOAuthResponse response;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        userDTO = new UserDTO();
        userDTO.setUsername("google 12345");
        userDTO.setEmail("test@gmail.com");
        userDTO.setName("Test User");
        userDTO.setRole("ROLE_USER");
        userDTO.setProviderId("12345");

        response = new MobileOAuthResponse(
            userDTO,
            "eyJhbGciOiJIUzI1NiJ9.test.access.token",
            "eyJhbGciOiJIUzI1NiJ9.test.refresh.token"
        );
    }

    @Test
    @DisplayName("Google OAuth 로그인 - 성공")
    void login_Google_Success() throws Exception {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("google");
        request.setAccessToken("google_access_token");
        
        when(mobileOAuthService.processOAuthLogin(any(MobileOAuthRequest.class)))
            .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v2/mobile-oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Apple OAuth 로그인 - Identity Token 사용")
    void login_Apple_Success() throws Exception {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("apple");
        request.setIdToken("apple_identity_token");
        request.setAccessToken("not_used_for_apple");
        
        UserDTO appleUser = new UserDTO();
        appleUser.setUsername("apple apple_user_id");
        appleUser.setEmail("test@icloud.com");
        appleUser.setName("Apple User");
        appleUser.setRole("ROLE_USER");
        appleUser.setProviderId("apple_user_id");
        
        MobileOAuthResponse appleResponse = new MobileOAuthResponse(
            appleUser,
            "eyJhbGciOiJIUzI1NiJ9.test.apple.access.token",
            "eyJhbGciOiJIUzI1NiJ9.test.apple.refresh.token"
        );
        
        when(mobileOAuthService.processOAuthLogin(any(MobileOAuthRequest.class)))
            .thenReturn(appleResponse);

        // when & then
        mockMvc.perform(post("/api/v2/mobile-oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@icloud.com"))
                .andExpect(jsonPath("$.user.name").value("Apple User"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Naver OAuth 로그인 - 성공")
    void login_Naver_Success() throws Exception {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("naver");
        request.setAccessToken("naver_access_token");
        
        UserDTO naverUser = new UserDTO();
        naverUser.setUsername("naver naver_user_id");
        naverUser.setEmail("test@naver.com");
        naverUser.setName("네이버 사용자");
        naverUser.setRole("ROLE_USER");
        naverUser.setProviderId("naver_user_id");
        
        MobileOAuthResponse naverResponse = new MobileOAuthResponse(
            naverUser,
            "eyJhbGciOiJIUzI1NiJ9.test.naver.access.token",
            "eyJhbGciOiJIUzI1NiJ9.test.naver.refresh.token"
        );
        
        when(mobileOAuthService.processOAuthLogin(any(MobileOAuthRequest.class)))
            .thenReturn(naverResponse);

        // when & then
        mockMvc.perform(post("/api/v2/mobile-oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@naver.com"))
                .andExpect(jsonPath("$.user.name").value("네이버 사용자"));
    }

    @Test
    @DisplayName("Kakao OAuth 로그인 - 성공")
    void login_Kakao_Success() throws Exception {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("kakao");
        request.setAccessToken("kakao_access_token");
        
        UserDTO kakaoUser = new UserDTO();
        kakaoUser.setUsername("kakao kakao_user_id");
        kakaoUser.setEmail("test@kakao.com");
        kakaoUser.setName("카카오 사용자");
        kakaoUser.setRole("ROLE_USER");
        kakaoUser.setProviderId("kakao_user_id");
        
        MobileOAuthResponse kakaoResponse = new MobileOAuthResponse(
            kakaoUser,
            "eyJhbGciOiJIUzI1NiJ9.test.kakao.access.token",
            "eyJhbGciOiJIUzI1NiJ9.test.kakao.refresh.token"
        );
        
        when(mobileOAuthService.processOAuthLogin(any(MobileOAuthRequest.class)))
            .thenReturn(kakaoResponse);

        // when & then
        mockMvc.perform(post("/api/v2/mobile-oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("test@kakao.com"))
                .andExpect(jsonPath("$.user.name").value("카카오 사용자"));
    }

    @Test
    @DisplayName("OAuth 로그인 - 필수 파라미터 누락")
    void login_MissingParameters() throws Exception {
        // given
        MobileOAuthRequest invalidRequest = new MobileOAuthRequest();
        // provider와 accessToken 누락

        // when & then
        mockMvc.perform(post("/api/v2/mobile-oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("OAuth 로그인 - 서비스 예외 처리")
    void login_ServiceException() throws Exception {
        // given
        request = new MobileOAuthRequest();
        request.setProvider("google");
        request.setAccessToken("invalid_token");
        
        when(mobileOAuthService.processOAuthLogin(any(MobileOAuthRequest.class)))
            .thenThrow(new RuntimeException("OAuth 인증 실패"));

        // when & then
        mockMvc.perform(post("/api/v2/mobile-oauth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("지원하는 OAuth 제공자 목록 조회")
    void getSupportedProviders() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v2/mobile-oauth/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("google"))
                .andExpect(jsonPath("$[1]").value("apple"))
                .andExpect(jsonPath("$[2]").value("naver"))
                .andExpect(jsonPath("$[3]").value("kakao"))
                .andExpect(jsonPath("$.length()").value(4));
    }
}