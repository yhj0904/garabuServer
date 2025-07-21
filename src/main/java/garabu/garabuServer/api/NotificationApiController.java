package garabu.garabuServer.api;

import garabu.garabuServer.service.NotificationService;
import garabu.garabuServer.service.FcmTokenService;
import garabu.garabuServer.dto.FcmTokenRegisterDTO;
import com.tencoding.garabu.dto.notification.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v2/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
@SecurityRequirement(name = "bearerAuth")
public class NotificationApiController {
    
    private final NotificationService notificationService;
    private final FcmTokenService fcmTokenService;
    
    @PostMapping("/token")
    @Operation(summary = "FCM 토큰 등록/업데이트")
    public ResponseEntity<Void> registerFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FcmTokenRegisterDTO request) {
        
        // 사용자 ID 설정
        request.setUserId(userDetails.getUsername());
        request.setAppId("garabu-app");
        
        fcmTokenService.registerOrUpdate(request);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/token/{deviceId}")
    @Operation(summary = "FCM 토큰 삭제")
    public ResponseEntity<Void> deleteFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String deviceId) {
        
        fcmTokenService.deleteFcmToken(userDetails.getUsername(), deviceId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/preferences")
    @Operation(summary = "알림 설정 조회")
    public ResponseEntity<NotificationPreferenceResponse> getNotificationPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {
        NotificationPreferenceResponse response = notificationService.getNotificationPreferences(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/preferences")
    @Operation(summary = "알림 설정 수정")
    public ResponseEntity<NotificationPreferenceResponse> updateNotificationPreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse response = notificationService.updateNotificationPreferences(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/budget-alerts")
    @Operation(summary = "예산 알림 목록 조회")
    public ResponseEntity<List<BudgetAlertResponse>> getBudgetAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<BudgetAlertResponse> response = notificationService.getBudgetAlerts(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/budget-alerts")
    @Operation(summary = "예산 알림 생성")
    public ResponseEntity<BudgetAlertResponse> createBudgetAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetAlertRequest request) {
        BudgetAlertResponse response = notificationService.createBudgetAlert(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/budget-alerts/{alertId}")
    @Operation(summary = "예산 알림 수정")
    public ResponseEntity<BudgetAlertResponse> updateBudgetAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long alertId,
            @Valid @RequestBody BudgetAlertRequest request) {
        BudgetAlertResponse response = notificationService.updateBudgetAlert(userDetails.getUsername(), alertId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/budget-alerts/{alertId}")
    @Operation(summary = "예산 알림 삭제")
    public ResponseEntity<Void> deleteBudgetAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long alertId) {
        notificationService.deleteBudgetAlert(userDetails.getUsername(), alertId);
        return ResponseEntity.ok().build();
    }
} 