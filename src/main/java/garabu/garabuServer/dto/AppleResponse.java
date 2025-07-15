package garabu.garabuServer.dto;

import java.util.Map;

public class AppleResponse implements OAuth2Response {
    private final Map<String, Object> attribute;

    public AppleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "apple";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email") != null ? attribute.get("email").toString() : null;
    }

    @Override
    public String getName() {
        // Apple은 이름 정보가 제한적이므로 이메일 기반으로 생성
        String email = getEmail();
        if (email != null) {
            return email.split("@")[0];
        }
        return "Apple User";
    }
}