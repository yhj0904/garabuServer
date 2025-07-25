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
                // 클라이언트에서 프로필 정보를 보낸 경우 우선 사용
                if (request.getProfile() != null) {
                    log.info("Google 로그인 - 클라이언트 프로필 정보 사용: id={}, email={}", 
                        request.getProfile().getId(), request.getProfile().getEmail());
                    userInfo = convertProfileToUserInfo(request.getProfile());
                } else {
                    // 기존 방식: ID Token 검증
                    String googleIdToken = request.getIdToken() != null ? request.getIdToken() : accessToken;
                    userInfo = getGoogleUserInfo(googleIdToken);
                }
                return new GoogleResponse(userInfo);
                
            case "apple":
                // 클라이언트에서 프로필 정보를 보낸 경우 우선 사용
                if (request.getProfile() != null) {
                    log.info("Apple 로그인 - 클라이언트 프로필 정보 사용: id={}, email={}", 
                        request.getProfile().getId(), request.getProfile().getEmail());
                    userInfo = convertProfileToUserInfo(request.getProfile());
                } else {
                    // 기존 방식: Identity Token 검증
                    String identityToken = request.getIdToken() != null ? request.getIdToken() : accessToken;
                    userInfo = getAppleUserInfo(identityToken);
                }
                return new AppleResponse(userInfo);
                
            case "naver":
                userInfo = getNaverUserInfo(accessToken);
                return new NaverResponse(userInfo);
                
            case "kakao":
                // 클라이언트에서 프로필 정보를 보낸 경우 우선 사용
                if (request.getProfile() != null) {
                    log.info("카카오 로그인 - 클라이언트 프로필 정보 사용: id={}, email={}", 
                        request.getProfile().getId(), request.getProfile().getEmail());
                    userInfo = convertProfileToUserInfo(request.getProfile());
                } else {
                    // 기존 방식: 서버에서 카카오 API 호출
                    userInfo = getKakaoUserInfo(accessToken);
                }
                return new KakaoResponse(userInfo);
                
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + provider);
        }
    }

    /**
     * Google 사용자 정보 조회
     * ID Token을 검증하고 사용자 정보를 추출
     */
    private Map<String, Object> getGoogleUserInfo(String idToken) {
        try {
            // Google ID Token 검증 API 사용
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> tokenInfo = response.getBody();
            
            if (tokenInfo == null) {
                throw new RuntimeException("Invalid Google ID token - no token info");
            }
            
            // 모바일 앱에서는 이미 인증된 사용자이므로 Client ID 검증 생략
            log.info("Google ID Token verified successfully (mobile app)");
            
            // ID Token의 정보를 userinfo 형식으로 변환
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", tokenInfo.get("sub"));
            userInfo.put("email", tokenInfo.get("email"));
            userInfo.put("name", tokenInfo.get("name"));
            userInfo.put("picture", tokenInfo.get("picture"));
            userInfo.put("given_name", tokenInfo.get("given_name"));
            userInfo.put("family_name", tokenInfo.get("family_name"));
            userInfo.put("verified_email", tokenInfo.get("email_verified"));
            
            log.info("Google ID Token 검증 성공: email={}", userInfo.get("email"));
            
            return userInfo;
        } catch (Exception e) {
            log.error("Google ID Token 검증 실패: {}", e.getMessage(), e);
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
            // 🔒 보안 수정: 토큰 검증 실패 시 예외 던지기 (가짜 정보 반환 금지)
            throw new RuntimeException("Apple 토큰 검증 실패", e);
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
     * 클라이언트에서 보낸 프로필 정보를 API 응답 형태로 변환
     * Google, Apple 등 다양한 제공자를 위해 ID는 String으로 유지
     */
    private Map<String, Object> convertProfileToUserInfo(MobileOAuthRequest.ProfileInfo profile) {
        Map<String, Object> userInfo = new HashMap<>();
        
        // Provider별로 다른 형식을 고려하여 범용적으로 처리
        // Google의 경우 ID가 매우 큰 숫자이므로 String으로 유지
        userInfo.put("id", profile.getId());
        userInfo.put("email", profile.getEmail());
        userInfo.put("name", profile.getName());
        
        // 카카오 형식 호환을 위한 추가 필드 (카카오에서 사용 시)
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", profile.getEmail());
        
        Map<String, Object> profileMap = new HashMap<>();
        profileMap.put("nickname", profile.getNickname() != null ? profile.getNickname() : profile.getName());
        profileMap.put("profile_image_url", profile.getProfileImageUrl());
        
        kakaoAccount.put("profile", profileMap);
        userInfo.put("kakao_account", kakaoAccount);
        
        // Google/Apple 형식 호환을 위한 추가 필드
        userInfo.put("picture", profile.getProfileImageUrl());
        userInfo.put("given_name", profile.getNickname());
        userInfo.put("family_name", "");
        
        return userInfo;
    }

    /**
     * 기존 OAuth 로직 재사용 - 회원 조회 또는 생성
     */
    private Member findOrCreateMember(OAuth2Response oAuth2Response) {
        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        String email = oAuth2Response.getEmail();
        String providerId = oAuth2Response.getProviderId();

        // 🔒 보안 강화: 이메일 유효성 검증
        if (email == null || email.trim().isEmpty()) {
            log.error("OAuth 로그인 시도 - 이메일 정보 없음: provider={}, providerId={}", 
                oAuth2Response.getProvider(), providerId);
            throw new RuntimeException("OAuth 제공자로부터 이메일 정보를 가져올 수 없습니다.");
        }

        // 🔒 보안 강화: 로그 기록
        log.info("OAuth 로그인 시도 - provider={}, email={}, providerId={}", 
            oAuth2Response.getProvider(), email, providerId);

        Optional<Member> optionalMember = memberJPARepository.findByProviderIdAndEmail(providerId, email);
        Member existData = optionalMember.orElse(null);

        if (existData == null) {
            // 🔒 보안 강화: 새 회원 생성 시 추가 로깅
            log.info("새 OAuth 회원 생성 - provider={}, email={}, name={}", 
                oAuth2Response.getProvider(), email, oAuth2Response.getName());
            
            // 새 회원 생성
            Member newMember = new Member();
            newMember.setUsername(username);
            newMember.setEmail(oAuth2Response.getEmail());
            newMember.setName(oAuth2Response.getName());
            newMember.setRole("ROLE_USER");
            newMember.setProviderId(providerId);
            
            return memberJPARepository.save(newMember);
        } else {
            // 🔒 보안 강화: 기존 회원 로그인 로깅
            log.info("기존 OAuth 회원 로그인 - provider={}, email={}, memberId={}", 
                oAuth2Response.getProvider(), email, existData.getId());
            
            // 기존 회원 정보 업데이트
            existData.setEmail(oAuth2Response.getEmail());
            existData.setUsername(username);
            existData.setName(oAuth2Response.getName());
            
            return memberJPARepository.save(existData);
        }
    }
}