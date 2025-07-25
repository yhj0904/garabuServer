package garabu.garabuServer.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {
    public void sendTo(String targetToken, String title, String body) {
        sendTo(targetToken, title, body, null);
    }
    
    public void sendTo(String targetToken, String title, String body, java.util.Map<String, String> data) {
        try {
            // 토큰 유효성 기본 검증
            if (targetToken == null || targetToken.trim().isEmpty()) {
                log.error("FCM 토큰이 비어있거나 null입니다.");
                throw new IllegalArgumentException("FCM 토큰이 비어있습니다.");
            }
            
            log.debug("FCM 발송 시작 - Token: {}, Title: {}, Body: {}", 
                targetToken.substring(0, Math.min(targetToken.length(), 20)) + "...", title, body);
            
            Message.Builder messageBuilder = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());
            
            // data 필드 추가 (앱에서 추가 정보를 받을 수 있도록)
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
                log.debug("FCM data 필드 추가: {}", data);
            }
            
            Message message = messageBuilder.build();
            
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 발송 성공 - Response: {}, Token: {}", response, 
                targetToken.substring(0, Math.min(targetToken.length(), 20)) + "...");
                
        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 실패 - Token: {}, ErrorCode: {}, Message: {}", 
                targetToken, e.getErrorCode(), e.getMessage(), e);
                
            // 에러 코드별 상세 로깅
            if (e.getErrorCode() != null) {
                String errorCodeName = e.getErrorCode().name();
                switch (errorCodeName) {
                    case "UNREGISTERED":
                        log.error("토큰이 등록되지 않았거나 만료되었습니다: {}", targetToken);
                        break;
                    case "INVALID_ARGUMENT":
                        log.error("잘못된 인자가 전달되었습니다. 토큰 형식을 확인하세요: {}", targetToken);
                        break;
                    case "SENDER_ID_MISMATCH":
                        log.error("발신자 ID가 일치하지 않습니다. Firebase 프로젝트 설정을 확인하세요.");
                        break;
                    case "QUOTA_EXCEEDED":
                        log.error("FCM 할당량을 초과했습니다.");
                        break;
                    default:
                        log.error("알 수 없는 FCM 오류: {}", errorCodeName);
                }
            }
            
            throw new RuntimeException("FCM 발송 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("FCM 발송 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("FCM 발송 실패: " + e.getMessage(), e);
        }
    }
}
