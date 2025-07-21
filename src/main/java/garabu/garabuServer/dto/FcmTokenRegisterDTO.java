package garabu.garabuServer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "FCM 토큰 등록 DTO")
public class FcmTokenRegisterDTO {

    @Schema(description = "앱 ID")
    private String appId;

    @Schema(description = "사용자 ID")
    private String userId;

    @Schema(description = "디바이스 ID")
    private String deviceId;
    
    @Schema(description = "디바이스 타입")
    private String deviceType;
    
    @Schema(description = "앱 버전")
    private String appVersion;
    
    @Schema(description = "OS 버전")
    private String osVersion;

    @Schema(description = "FCM 토큰")
    @JsonProperty("token")
    private String fcmToken;
    
    // token으로도 fcmToken을 설정할 수 있도록 setter 추가
    public void setToken(String token) {
        this.fcmToken = token;
    }
}
