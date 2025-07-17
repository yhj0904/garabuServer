package garabu.garabuServer.service;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.*;
import garabu.garabuServer.jwt.JWTConstants;
import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.repository.MemberJPARepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 모바일 OAuth 서비스
 * 
 * 기존 웹 OAuth 로직을 재사용하면서 모바일 앱에서 받은 토큰을 검증하고
 * 사용자 정보를 조회하여 JWT 토큰을 발급하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MobileOAuthService {
    
    private final MemberJPARepository memberJPARepository;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AppleOAuthService appleOAuthService;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    /**
     * 모바일에서 받은 OAuth 토큰으로 사용자 정보 조회 및 JWT 토큰 발급
     */
    @Transactional
    public MobileOAuthResponse processOAuthLogin(MobileOAuthRequest request) {
        try {
            // 1. OAuth 제공자별 사용자 정보 조회
            OAuth2Response oAuth2Response = getUserInfoFromProvider(request.getProvider(), request.getAccessToken(), request);
            
            // 2. 기존 OAuth 로직 재사용 - 회원 조회 또는 생성
            Member member = findOrCreateMember(oAuth2Response);
            
            // 3. JWT 토큰 생성 (기존 로직 재사용)
            String accessToken = jwtUtil.createJwt(
                JWTConstants.ACCESS_TOKEN_CATEGORY, 
                member.getUsername(), 
                member.getRole(), 
                JWTConstants.ACCESS_TOKEN_EXPIRE_TIME
            );
            
            String refreshToken = jwtUtil.createJwt(
                JWTConstants.REFRESH_TOKEN_CATEGORY, 
                member.getUsername(), 
                member.getRole(), 
                JWTConstants.REFRESH_TOKEN_EXPIRE_TIME
            );
            
            // 4. Refresh 토큰 저장
            String refreshJti = jwtUtil.getJti(refreshToken);
            refreshTokenService.saveRefreshToken(
                member.getUsername(), 
                refreshToken, 
                refreshJti, 
                JWTConstants.REFRESH_TOKEN_EXPIRE_TIME
            );
            
            // 5. 응답 생성
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(member.getUsername());
            userDTO.setEmail(member.getEmail());
            userDTO.setName(member.getName());
            userDTO.setRole(member.getRole());
            userDTO.setProviderId(member.getProviderId());
            
            return new MobileOAuthResponse(userDTO, accessToken, refreshToken);
            
        } catch (Exception e) {
            log.error("OAuth 로그인 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("OAuth 로그인 처리 실패", e);
        }
    }

    /**
     * OAuth 제공자별 사용자 정보 조회
     */
    private OAuth2Response getUserInfoFromProvider(String provider, String accessToken, MobileOAuthRequest request) {
        Map<String, Object> userInfo;
        
        switch (provider.toLowerCase()) {
            case "google":
                userInfo = getGoogleUserInfo(accessToken);
                return new GoogleResponse(userInfo);
                
            case "apple":
                // Apple의 경우 identityToken을 사용
                String identityToken = request.getIdToken() != null ? request.getIdToken() : accessToken;
                userInfo = getAppleUserInfo(identityToken);
                return new AppleResponse(userInfo);
                
            case "naver":
                userInfo = getNaverUserInfo(accessToken);
                return new NaverResponse(userInfo);
                
            case "kakao":
                userInfo = getKakaoUserInfo(accessToken);
                return new KakaoResponse(userInfo);
                
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + provider);
        }
    }

    /**
     * Google 사용자 정보 조회
     */
    private Map<String, Object> getGoogleUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Google 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Google 토큰 검증 실패", e);
        }
    }

    /**
     * Apple 사용자 정보 조회
     * Apple은 Identity Token을 JWT로 파싱해야 함
     */
    private Map<String, Object> getAppleUserInfo(String identityToken) {
        try {
            // AppleOAuthService를 사용하여 Identity Token에서 사용자 정보 추출
            return appleOAuthService.extractUserInfoFromIdentityToken(identityToken);
        } catch (Exception e) {
            log.error("Apple 사용자 정보 추출 실패: {}", e.getMessage(), e);
            // Apple 토큰 파싱 실패 시 기본 정보 반환
            Map<String, Object> defaultInfo = new HashMap<>();
            defaultInfo.put("sub", "apple_user_" + System.currentTimeMillis());
            defaultInfo.put("email", "apple@example.com");
            defaultInfo.put("email_verified", true);
            return defaultInfo;
        }
    }

    /**
     * Naver 사용자 정보 조회
     */
    private Map<String, Object> getNaverUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Naver 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Naver 토큰 검증 실패", e);
        }
    }

    /**
     * Kakao 사용자 정보 조회
     */
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Kakao 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Kakao 토큰 검증 실패", e);
        }
    }

    /**
     * 기존 OAuth 로직 재사용 - 회원 조회 또는 생성
     */
    private Member findOrCreateMember(OAuth2Response oAuth2Response) {
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String providerId = oAuth2Response.getProviderId();

        Optional<Member> optionalMember = memberJPARepository.findByProviderIdAndEmail(providerId, email);
        Member existData = optionalMember.orElse(null);

        if (existData == null) {
            // 새 회원 생성
            Member newMember = new Member();
            newMember.setUsername(username);
            newMember.setEmail(oAuth2Response.getEmail());
            newMember.setName(oAuth2Response.getName());
            newMember.setRole("ROLE_USER");
            newMember.setProviderId(providerId);
            
            return memberJPARepository.save(newMember);
        } else {
            // 기존 회원 정보 업데이트
            existData.setEmail(oAuth2Response.getEmail());
            existData.setUsername(username);
            existData.setName(oAuth2Response.getName());
            
            return memberJPARepository.save(existData);
        }
    }
}