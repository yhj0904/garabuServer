package garabu.garabuServer.api;

import garabu.garabuServer.dto.FcmSendRequestDTO;
import garabu.garabuServer.dto.FcmTokenRegisterDTO;
import garabu.garabuServer.service.FcmSendService;
import garabu.garabuServer.service.FcmTokenService;
import garabu.garabuServer.service.PushNotificationService;
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
 * 알림 API 컨트롤러
 * 
 * FCM 푸시 알림 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v2/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "알림 API")
public class NotificationApiController {
    
    private final FcmSendService fcmSendService;
    private final FcmTokenService fcmTokenService;
    private final PushNotificationService pushNotificationService;
    
    /**
     * FCM 토큰 등록
     * 
     * @param request FCM 토큰 등록 요청
     * @return 등록 결과
     */
    @PostMapping("/token")
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    public ResponseEntity<Map<String, Object>> registerFcmToken(@Valid @RequestBody FcmTokenRegisterDTO request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("FCM 토큰 등록 요청: user={}, deviceId={}", username, request.getDeviceId());
            
            // 사용자 ID를 username으로 설정
            request.setUserId(username);
            
            fcmTokenService.registerFcmToken(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "FCM 토큰이 성공적으로 등록되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("FCM 토큰 등록 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "FCM 토큰 등록에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * FCM 토큰 삭제
     * 
     * @param deviceId 디바이스 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/token/{deviceId}")
    @Operation(summary = "FCM 토큰 삭제", description = "사용자의 FCM 토큰을 삭제합니다.")
    public ResponseEntity<Map<String, Object>> deleteFcmToken(
            @Parameter(description = "디바이스 ID") @PathVariable String deviceId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("FCM 토큰 삭제 요청: user={}, deviceId={}", username, deviceId);
            
            fcmTokenService.deleteFcmToken(username, deviceId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "FCM 토큰이 성공적으로 삭제되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("FCM 토큰 삭제 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "FCM 토큰 삭제에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * 푸시 알림 전송
     * 
     * @param request 푸시 알림 요청
     * @return 전송 결과
     */
    @PostMapping("/send")
    @Operation(summary = "푸시 알림 전송", description = "사용자에게 푸시 알림을 전송합니다.")
    public ResponseEntity<Map<String, Object>> sendPushNotification(@Valid @RequestBody FcmSendRequestDTO request) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("푸시 알림 전송 요청: user={}, title={}, targets={}", username, request.getNoticeTitle(), request.getSendUserList());
            
            // 발송자 정보 설정
            request.setUserId(username);
            
            fcmSendService.sendPush(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "푸시 알림이 성공적으로 전송되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("푸시 알림 전송 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "푸시 알림 전송에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * 일반 알림 전송
     * 
     * @param request 일반 알림 요청
     * @return 전송 결과
     */
    @PostMapping("/send/general")
    @Operation(summary = "일반 알림 전송", description = "일반 알림을 전송합니다.")
    public ResponseEntity<Map<String, Object>> sendGeneralNotification(@RequestBody GeneralNotificationRequest request) {
        try {
            log.info("일반 알림 전송 요청: title={}, targets={}", request.getTitle(), request.getUserIds());
            
            pushNotificationService.sendGeneralNotification(
                request.getUserIds(),
                request.getTitle(),
                request.getBody(),
                request.getAction()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "일반 알림이 성공적으로 전송되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("일반 알림 전송 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "일반 알림 전송에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * 예약 알림 전송
     * 
     * @param request 예약 알림 요청
     * @return 전송 결과
     */
    @PostMapping("/send/scheduled")
    @Operation(summary = "예약 알림 전송", description = "예약 알림을 등록합니다.")
    public ResponseEntity<Map<String, Object>> sendScheduledNotification(@RequestBody ScheduledNotificationRequest request) {
        try {
            log.info("예약 알림 등록 요청: title={}, scheduledTime={}, targets={}", 
                request.getTitle(), request.getScheduledTime(), request.getUserIds());
            
            pushNotificationService.sendScheduledNotification(
                request.getUserIds(),
                request.getTitle(),
                request.getBody(),
                request.getAction(),
                request.getScheduledTime()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "예약 알림이 성공적으로 등록되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("예약 알림 등록 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "예약 알림 등록에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * 테스트 알림 전송
     * 
     * @return 전송 결과
     */
    @PostMapping("/send/test")
    @Operation(summary = "테스트 알림 전송", description = "현재 사용자에게 테스트 알림을 전송합니다.")
    public ResponseEntity<Map<String, Object>> sendTestNotification() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("테스트 알림 전송 요청: user={}", username);
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId("garabu-app")
                .noticeTitle("테스트 알림")
                .noticeBody("가라부 앱 푸시 알림 테스트입니다.")
                .noticeAction("open_main")
                .userId(username)
                .userNm("테스트")
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(username)
                .build();
            
            fcmSendService.sendPush(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "테스트 알림이 성공적으로 전송되었습니다.",
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            log.error("테스트 알림 전송 실패", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "테스트 알림 전송에 실패했습니다: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
    
    /**
     * 일반 알림 요청 DTO
     */
    public static class GeneralNotificationRequest {
        private List<Long> userIds;
        private String title;
        private String body;
        private String action;
        
        // Getters and setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
    
    /**
     * 예약 알림 요청 DTO
     */
    public static class ScheduledNotificationRequest {
        private List<Long> userIds;
        private String title;
        private String body;
        private String action;
        private LocalDateTime scheduledTime;
        
        // Getters and setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public LocalDateTime getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
    }
}