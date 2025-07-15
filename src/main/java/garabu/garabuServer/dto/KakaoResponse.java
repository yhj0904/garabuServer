package garabu.garabuServer.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {
    private final Map<String, Object> attribute;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        if (kakaoAccount != null) {
            return kakaoAccount.get("email") != null ? kakaoAccount.get("email").toString() : null;
        }
        return null;
    }

    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                return profile.get("nickname") != null ? profile.get("nickname").toString() : null;
            }
        }
        return "Kakao User";
    }
}