package garabu.garabuServer.service;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 통합 푸시 알림 서비스
 * 
 * FCM 푸시 알림을 전송합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedPushNotificationService {
    
    private final PushNotificationService fcmPushNotificationService;
    
    /**
     * 새 거래 내역 추가 알림
     */
    public void sendNewTransactionNotification(Ledger ledger, Member author) {
        log.debug("Sending FCM notification for new transaction");
        fcmPushNotificationService.sendNewTransactionNotification(ledger, author);
    }
    
    /**
     * 가계부 멤버 초대 알림
     */
    public void sendBookInvitationNotification(Long bookId, Member invitedMember, Member inviterMember, String role) {
        log.debug("Sending FCM notification for book invitation");
        fcmPushNotificationService.sendBookInvitationNotification(bookId, invitedMember, inviterMember, role);
    }
    
    /**
     * 가계부 멤버 제거 알림
     */
    public void sendMemberRemovedNotification(Long bookId, Member removedMember, Member removerMember) {
        log.debug("Sending FCM notification for member removal");
        fcmPushNotificationService.sendMemberRemovedNotification(bookId, removedMember, removerMember);
    }
    
    /**
     * 가계부 권한 변경 알림
     */
    public void sendRoleChangedNotification(Long bookId, Member targetMember, Member changerMember, String newRole) {
        log.debug("Sending FCM notification for role change");
        fcmPushNotificationService.sendRoleChangedNotification(bookId, targetMember, changerMember, newRole);
    }
    
    /**
     * 가계부 멤버 탈퇴 알림
     */
    public void sendMemberLeftNotification(Long bookId, Member leftMember) {
        log.debug("Sending FCM notification for member leaving");
        fcmPushNotificationService.sendMemberLeftNotification(bookId, leftMember);
    }
    
    /**
     * 친구 요청 알림
     */
    public void sendFriendRequestNotification(Long receiverId, Long senderId, String senderName) {
        log.debug("Sending FCM notification for friend request");
        fcmPushNotificationService.sendFriendRequestNotification(receiverId, senderId, senderName);
    }
    
    /**
     * 친구 수락 알림
     */
    public void sendFriendAcceptNotification(Long receiverId, Long acceptorId, String acceptorName) {
        log.debug("Sending FCM notification for friend acceptance");
        fcmPushNotificationService.sendFriendAcceptNotification(receiverId, acceptorId, acceptorName);
    }
    
    /**
     * 예산 초과 알림
     */
    public void sendBudgetExceededNotification(Long bookId, int currentAmount, int budgetAmount, int percentage) {
        log.debug("Sending FCM notification for budget exceeded");
        fcmPushNotificationService.sendBudgetExceededNotification(bookId, currentAmount, budgetAmount, percentage);
    }
    
    /**
     * 일반 알림 전송
     */
    public void sendGeneralNotification(List<Long> userIds, String title, String body, String action) {
        log.debug("Sending FCM general notification");
        fcmPushNotificationService.sendGeneralNotification(userIds, title, body, action);
    }
    
    /**
     * 예약 알림 전송 (FCM only)
     */
    public void sendScheduledNotification(List<Long> userIds, String title, String body, String action, LocalDateTime scheduledTime) {
        log.debug("Sending FCM scheduled notification");
        fcmPushNotificationService.sendScheduledNotification(userIds, title, body, action, scheduledTime);
    }
}