package garabu.garabuServer.dto;

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

    @Schema(description = "FCM 토큰")
    private String fcmToken;
}
