package garabu.garabuServer.service;

import garabu.garabuServer.config.NotificationConfig;
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
 * 설정에 따라 FCM 또는 Expo 푸시 알림을 전송합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedPushNotificationService {
    
    private final NotificationConfig notificationConfig;
    private final PushNotificationService fcmPushNotificationService;
    private final ExpoPushNotificationService expoPushNotificationService;
    
    /**
     * 새 거래 내역 추가 알림
     */
    public void sendNewTransactionNotification(Ledger ledger, Member author) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for new transaction");
            expoPushNotificationService.sendNewTransactionNotification(ledger, author);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for new transaction");
            fcmPushNotificationService.sendNewTransactionNotification(ledger, author);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 가계부 멤버 초대 알림
     */
    public void sendBookInvitationNotification(Long bookId, Member invitedMember, Member inviterMember, String role) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for book invitation");
            expoPushNotificationService.sendBookInvitationNotification(bookId, invitedMember, inviterMember, role);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for book invitation");
            fcmPushNotificationService.sendBookInvitationNotification(bookId, invitedMember, inviterMember, role);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 가계부 멤버 제거 알림
     */
    public void sendMemberRemovedNotification(Long bookId, Member removedMember, Member removerMember) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for member removal");
            expoPushNotificationService.sendMemberRemovedNotification(bookId, removedMember, removerMember);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for member removal");
            fcmPushNotificationService.sendMemberRemovedNotification(bookId, removedMember, removerMember);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 가계부 권한 변경 알림
     */
    public void sendRoleChangedNotification(Long bookId, Member targetMember, Member changerMember, String newRole) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for role change");
            expoPushNotificationService.sendRoleChangedNotification(bookId, targetMember, changerMember, newRole);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for role change");
            fcmPushNotificationService.sendRoleChangedNotification(bookId, targetMember, changerMember, newRole);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 가계부 멤버 탈퇴 알림
     */
    public void sendMemberLeftNotification(Long bookId, Member leftMember) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for member leaving");
            expoPushNotificationService.sendMemberLeftNotification(bookId, leftMember);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for member leaving");
            fcmPushNotificationService.sendMemberLeftNotification(bookId, leftMember);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 친구 요청 알림
     */
    public void sendFriendRequestNotification(Long receiverId, Long senderId, String senderName) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for friend request");
            expoPushNotificationService.sendFriendRequestNotification(receiverId, senderId, senderName);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for friend request");
            fcmPushNotificationService.sendFriendRequestNotification(receiverId, senderId, senderName);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 친구 수락 알림
     */
    public void sendFriendAcceptNotification(Long receiverId, Long acceptorId, String acceptorName) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for friend acceptance");
            expoPushNotificationService.sendFriendAcceptNotification(receiverId, acceptorId, acceptorName);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for friend acceptance");
            fcmPushNotificationService.sendFriendAcceptNotification(receiverId, acceptorId, acceptorName);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 예산 초과 알림
     */
    public void sendBudgetExceededNotification(Long bookId, int currentAmount, int budgetAmount, int percentage) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo notification for budget exceeded");
            expoPushNotificationService.sendBudgetExceededNotification(bookId, currentAmount, budgetAmount, percentage);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM notification for budget exceeded");
            fcmPushNotificationService.sendBudgetExceededNotification(bookId, currentAmount, budgetAmount, percentage);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 일반 알림 전송
     */
    public void sendGeneralNotification(List<Long> userIds, String title, String body, String action) {
        if (notificationConfig.isExpoEnabled()) {
            log.debug("Sending Expo general notification");
            expoPushNotificationService.sendGeneralNotification(userIds, title, body, action);
        } else if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM general notification");
            fcmPushNotificationService.sendGeneralNotification(userIds, title, body, action);
        } else {
            log.warn("No notification provider enabled");
        }
    }
    
    /**
     * 예약 알림 전송 (FCM only)
     */
    public void sendScheduledNotification(List<Long> userIds, String title, String body, String action, LocalDateTime scheduledTime) {
        if (notificationConfig.isFcmEnabled()) {
            log.debug("Sending FCM scheduled notification");
            fcmPushNotificationService.sendScheduledNotification(userIds, title, body, action, scheduledTime);
        } else {
            log.warn("Scheduled notifications are only supported with FCM");
        }
    }
}