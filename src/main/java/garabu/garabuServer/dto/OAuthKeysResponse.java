package garabu.garabuServer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthKeysResponse {
    
    private KakaoKeys kakao;
    private GoogleKeys google;
    private AppleKeys apple;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoKeys {
        private String nativeAppKey;
        private String restApiKey;
        private String javascriptKey;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleKeys {
        private String androidClientId;
        private String iosClientId;
        private String webClientId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppleKeys {
        private String teamId;
        private String clientId;
        private String keyId;
    }
}