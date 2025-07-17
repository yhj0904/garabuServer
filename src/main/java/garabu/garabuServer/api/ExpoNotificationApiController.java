package garabu.garabuServer.api;

import garabu.garabuServer.service.ExpoTokenService;
import garabu.garabuServer.service.ExpoPushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Expo 알림 API 컨트롤러
 * 
 * Expo 푸시 알림 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v2/expo/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expo Notification", description = "Expo 푸시 알림 API")
public class ExpoNotificationApiController {
    
    private final ExpoTokenService expoTokenService;
    private final ExpoPushNotificationService expoPushNotificationService;
    
    /**
     * Expo 푸시 토큰 등록
     * 
     * @param request Expo 토큰 등록 요청
     * @return 등록 결과
     */
    @PostMapping("/token")
    @Operation(summary = "Expo 푸시 토큰 등록", description = "사용자의 Expo 푸시 토큰을 등록합니다.")
    public ResponseEntity<Map<String, Object>> registerExpoToken(@Valid @RequestBody ExpoTokenRegisterRequest request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = Long.parseLong(username);
            
            log.info("Expo 토큰 등록 요청: userId={}, deviceId={}, token={}", userId, request.getDeviceId(), request.getExpoPushToken());
            
            expoTokenService.registerToken(userId, request.getDeviceId(), request.getExpoPushToken());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Expo 푸시 토큰이 성공적으로 등록되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Expo 토큰 등록 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Expo 토큰 등록에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Expo 푸시 토큰 삭제
     * 
     * @param deviceId 디바이스 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/token/{deviceId}")
    @Operation(summary = "Expo 푸시 토큰 삭제", description = "사용자의 Expo 푸시 토큰을 삭제합니다.")
    public ResponseEntity<Map<String, Object>> deleteExpoToken(
            @Parameter(description = "디바이스 ID") @PathVariable String deviceId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = Long.parseLong(username);
            
            log.info("Expo 토큰 삭제 요청: userId={}, deviceId={}", userId, deviceId);
            
            expoTokenService.deleteToken(userId, deviceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Expo 토큰이 성공적으로 삭제되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Expo 토큰 삭제 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Expo 토큰 삭제에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * 테스트 알림 전송
     * 
     * @return 전송 결과
     */
    @PostMapping("/test")
    @Operation(summary = "Expo 테스트 알림 전송", description = "현재 사용자에게 Expo 테스트 알림을 전송합니다.")
    public ResponseEntity<Map<String, Object>> sendTestNotification() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Long userId = Long.parseLong(username);
            
            log.info("Expo 테스트 알림 전송 요청: userId={}", userId);
            
            List<String> tokens = expoTokenService.getActiveTokensByUserId(userId);
            
            if (tokens.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "등록된 Expo 토큰이 없습니다.",
                    "timestamp", LocalDateTime.now()
                ));
            }
            
            expoPushNotificationService.sendGeneralNotification(
                List.of(userId),
                "테스트 알림",
                "가라부 앱 Expo 푸시 알림 테스트입니다.",
                "open_main"
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Expo 테스트 알림이 성공적으로 전송되었습니다.",
                "tokenCount", tokens.size(),
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("Expo 테스트 알림 전송 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Expo 테스트 알림 전송에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * Expo 토큰 등록 요청 DTO
     */
    public static class ExpoTokenRegisterRequest {
        @jakarta.validation.constraints.NotBlank(message = "디바이스 ID는 필수입니다")
        private String deviceId;
        
        @jakarta.validation.constraints.NotBlank(message = "Expo 푸시 토큰은 필수입니다")
        @jakarta.validation.constraints.Pattern(regexp = "ExponentPushToken\\[.*\\]", message = "유효하지 않은 Expo 푸시 토큰 형식입니다")
        private String expoPushToken;
        
        private String platform; // ios, android
        private String appVersion;
        
        // Getters and setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getExpoPushToken() { return expoPushToken; }
        public void setExpoPushToken(String expoPushToken) { this.expoPushToken = expoPushToken; }
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getAppVersion() { return appVersion; }
        public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
    }
}