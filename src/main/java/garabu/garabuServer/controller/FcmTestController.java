package garabu.garabuServer.controller;

import garabu.garabuServer.service.FcmService;
import garabu.garabuServer.service.PushNotificationService;
import garabu.garabuServer.repository.FcmTokenRepository;
import garabu.garabuServer.domain.FcmUserToken;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/test/fcm")
@RequiredArgsConstructor
public class FcmTestController {
    
    private final FcmService fcmService;
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;
    private final PushNotificationService pushNotificationService;
    
    @PostMapping("/send-direct")
    public ResponseEntity<Map<String, Object>> sendDirectNotification(
            @RequestParam String token,
            @RequestParam(defaultValue = "Test Notification") String title,
            @RequestParam(defaultValue = "This is a test notification from Garabu server") String body) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== FCM Direct Send Test ===");
            log.info("Token: {}", token);
            log.info("Title: {}", title);
            log.info("Body: {}", body);
            
            Map<String, String> data = new HashMap<>();
            data.put("type", "test");
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            fcmService.sendTo(token, title, body, data);
            
            response.put("success", true);
            response.put("message", "FCM notification sent successfully");
            response.put("token", token);
            log.info("FCM Direct Send Success");
            
        } catch (Exception e) {
            log.error("FCM Direct Send Failed: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorClass", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                response.put("cause", e.getCause().getMessage());
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tokens/{userId}")
    public ResponseEntity<Map<String, Object>> getUserTokens(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FcmUserToken> tokens = fcmTokenRepository.findAll().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .collect(Collectors.toList());
            
            response.put("userId", userId);
            response.put("tokenCount", tokens.size());
            response.put("tokens", tokens.stream().map(t -> {
                Map<String, Object> tokenInfo = new HashMap<>();
                tokenInfo.put("tokenId", t.getTokenId());
                tokenInfo.put("appId", t.getAppId());
                tokenInfo.put("deviceId", t.getDeviceId());
                tokenInfo.put("fcmToken", t.getFcmToken());
                tokenInfo.put("useAt", t.getUseAt());
                tokenInfo.put("regDt", t.getRegDt());
                return tokenInfo;
            }).collect(Collectors.toList()));
            
        } catch (Exception e) {
            log.error("Failed to get user tokens: ", e);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-to-user")
    public ResponseEntity<Map<String, Object>> sendToUser(
            @RequestParam String userId,
            @RequestParam(defaultValue = "Test Notification") String title,
            @RequestParam(defaultValue = "This is a test notification") String body) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Find user's active token
            Optional<FcmUserToken> tokenOpt = fcmTokenRepository
                .findTopByAppIdAndUserIdAndUseAtOrderByTokenIdDesc("garabu-app", userId, "Y");
            
            if (tokenOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "No active FCM token found for user: " + userId);
                return ResponseEntity.ok(response);
            }
            
            FcmUserToken token = tokenOpt.get();
            log.info("Found token for user {}: {}", userId, token.getFcmToken());
            
            Map<String, String> data = new HashMap<>();
            data.put("type", "test");
            data.put("userId", userId);
            data.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            fcmService.sendTo(token.getFcmToken(), title, body, data);
            
            response.put("success", true);
            response.put("message", "Notification sent to user");
            response.put("userId", userId);
            response.put("deviceId", token.getDeviceId());
            response.put("token", token.getFcmToken());
            
        } catch (Exception e) {
            log.error("Failed to send notification to user: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorClass", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/test-push-service")
    public ResponseEntity<Map<String, Object>> testPushService(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Member sender = memberRepository.findOne(senderId);
            if (sender == null) {
                throw new IllegalArgumentException("Sender not found: " + senderId);
            }
            
            Member receiver = memberRepository.findOne(receiverId);
            if (receiver == null) {
                throw new IllegalArgumentException("Receiver not found: " + receiverId);
            }
            
            // Test friend request notification
            pushNotificationService.sendFriendRequestNotification(
                receiverId, senderId, sender.getName()
            );
            
            response.put("success", true);
            response.put("message", "Push notification service test initiated");
            response.put("sender", sender.getName());
            response.put("receiver", receiver.getName());
            
        } catch (Exception e) {
            log.error("Push service test failed: ", e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/firebase-status")
    public ResponseEntity<Map<String, Object>> getFirebaseStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if Firebase is initialized
            response.put("firebaseInitialized", true);
            
            // Get token statistics
            List<FcmUserToken> allTokens = fcmTokenRepository.findAll();
            long activeTokens = allTokens.stream()
                .filter(t -> "Y".equals(t.getUseAt()))
                .count();
            
            response.put("totalTokens", allTokens.size());
            response.put("activeTokens", activeTokens);
            response.put("inactiveTokens", allTokens.size() - activeTokens);
            
            // Group by app ID
            Map<String, Long> tokensByApp = allTokens.stream()
                .filter(t -> "Y".equals(t.getUseAt()))
                .collect(Collectors.groupingBy(
                    t -> t.getAppId() != null ? t.getAppId() : "null",
                    Collectors.counting()
                ));
            response.put("tokensByApp", tokensByApp);
            
            // Recent tokens
            List<Map<String, Object>> recentTokens = allTokens.stream()
                .filter(t -> "Y".equals(t.getUseAt()))
                .sorted((a, b) -> b.getRegDt().compareTo(a.getRegDt()))
                .limit(5)
                .map(t -> {
                    Map<String, Object> tokenInfo = new HashMap<>();
                    tokenInfo.put("userId", t.getUserId());
                    tokenInfo.put("deviceId", t.getDeviceId());
                    tokenInfo.put("regDt", t.getRegDt());
                    tokenInfo.put("tokenPreview", t.getFcmToken() != null && t.getFcmToken().length() > 20 
                        ? t.getFcmToken().substring(0, 20) + "..." 
                        : t.getFcmToken());
                    return tokenInfo;
                })
                .collect(Collectors.toList());
            response.put("recentTokens", recentTokens);
            
        } catch (Exception e) {
            log.error("Failed to get Firebase status: ", e);
            response.put("error", e.getMessage());
            response.put("firebaseInitialized", false);
        }
        
        return ResponseEntity.ok(response);
    }
}