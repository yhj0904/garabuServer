package garabu.garabuServer.api;

import garabu.garabuServer.dto.OAuthKeysResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OAuth 키 관리 API 컨트롤러
 * 
 * 클라이언트 앱에서 사용할 OAuth 키를 안전하게 제공합니다.
 * 민감한 정보는 서버에서 관리하고 필요한 public 키만 클라이언트에 전달합니다.
 */
@RestController
@RequestMapping("/api/v2/oauth-keys")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth Keys", description = "OAuth 인증 키 관리 API")
public class OAuthKeysApiController {
    
    // Kakao OAuth Keys
    @Value("${oauth.kakao.native-app-key:9232996cd9a91757d2e423adfb12254a}")
    private String kakaoNativeAppKey;
    
    @Value("${oauth.kakao.rest-api-key:}")
    private String kakaoRestApiKey;
    
    @Value("${oauth.kakao.javascript-key:}")
    private String kakaoJavascriptKey;
    
    // Google OAuth Keys
    @Value("${oauth.google.android-client-id:392078217032-lsgofa1o9vqss83dlnjhro4o2jppvkq3.apps.googleusercontent.com}")
    private String googleAndroidClientId;
    
    @Value("${oauth.google.ios-client-id:392078217032-rhofbdds2f1a1i41umvd6q39igjvjech.apps.googleusercontent.com}")
    private String googleIosClientId;
    
    @Value("${oauth.google.web-client-id:}")
    private String googleWebClientId;
    
    // Apple OAuth Keys
    @Value("${oauth.apple.team-id:}")
    private String appleTeamId;
    
    @Value("${oauth.apple.client-id:}")
    private String appleClientId;
    
    @Value("${oauth.apple.key-id:}")
    private String appleKeyId;
    
    /**
     * 모바일 앱에서 사용할 OAuth 키 조회
     * 
     * @return OAuth 제공자별 public 키 정보
     */
    @GetMapping("/mobile")
    @Operation(summary = "모바일 OAuth 키 조회", description = "모바일 앱에서 사용할 OAuth 제공자별 public 키를 조회합니다.")
    public ResponseEntity<OAuthKeysResponse> getMobileOAuthKeys() {
        log.info("모바일 OAuth 키 조회 요청");
        
        OAuthKeysResponse response = OAuthKeysResponse.builder()
            .kakao(OAuthKeysResponse.KakaoKeys.builder()
                .nativeAppKey(kakaoNativeAppKey)
                .restApiKey(kakaoRestApiKey)
                .javascriptKey(kakaoJavascriptKey)
                .build())
            .google(OAuthKeysResponse.GoogleKeys.builder()
                .androidClientId(googleAndroidClientId)
                .iosClientId(googleIosClientId)
                .webClientId(googleWebClientId)
                .build())
            .apple(OAuthKeysResponse.AppleKeys.builder()
                .teamId(appleTeamId)
                .clientId(appleClientId)
                .keyId(appleKeyId)
                .build())
            .build();
        
        return ResponseEntity.ok(response);
    }
}