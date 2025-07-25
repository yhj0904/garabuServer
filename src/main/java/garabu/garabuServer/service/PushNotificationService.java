package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.dto.FcmSendRequestDTO;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 푸시 알림 서비스
 * 
 * 가계부 앱의 다양한 상황에 맞는 푸시 알림을 전송합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {
    
    private final FcmSendService fcmSendService;
    private final BookRepository bookRepository;
    private final UserBookJpaRepository userBookJpaRepository;
    
    private static final String APP_ID = "garabu-app";
    
    /**
     * 새 거래 내역 추가 알림
     * 
     * @param ledger 새로 추가된 거래 내역
     * @param author 거래를 추가한 사용자
     */
    @Transactional(readOnly = true)
    public void sendNewTransactionNotification(Ledger ledger, Member author) {
        try {
            Book book = bookRepository.findById(ledger.getBook().getId())
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
            
            // 가계부 멤버들 조회 (작성자 제외)
            List<UserBook> bookMembers = userBookJpaRepository.findByBookId(ledger.getBook().getId())
                .stream()
                .filter(ub -> !ub.getMember().getId().equals(author.getId()))
                .collect(Collectors.toList());
            
            if (bookMembers.isEmpty()) {
                log.info("알림 대상자가 없습니다. BookId: {}", ledger.getBook().getId());
                return;
            }
            
            String userIds = bookMembers.stream()
                .map(ub -> String.valueOf(ub.getMember().getId()))
                .collect(Collectors.joining(","));
            
            String transactionType = ledger.getAmountType() == garabu.garabuServer.domain.AmountType.INCOME ? "수입" : "지출";
            String title = String.format("새 %s 추가", transactionType);
            String body = String.format("%s님이 \"%s\" 가계부에 새 %s을 추가했습니다: %s (₩%,d)", 
                author.getName(), book.getTitle(), transactionType, ledger.getDescription(), ledger.getAmount());
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction(String.format("transaction:%d:%d", ledger.getBook().getId(), ledger.getId()))
                .userId(String.valueOf(author.getId()))
                .userNm(author.getName())
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(userIds)
                .build();
            
            fcmSendService.sendPush(request);
            log.info("새 거래 알림 발송 완료. BookId: {}, UserCount: {}", ledger.getBook().getId(), bookMembers.size());
            
        } catch (Exception e) {
            log.error("새 거래 알림 발송 실패", e);
        }
    }
    
    /**
     * 가계부 멤버 초대 알림
     * 
     * @param bookId 가계부 ID
     * @param invitedMember 초대된 멤버
     * @param inviterMember 초대한 멤버
     * @param role 부여된 역할
     */
    public void sendBookInvitationNotification(Long bookId, Member invitedMember, Member inviterMember, String role) {
        try {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
            
            String roleText = "EDITOR".equals(role) ? "편집자" : "조회자";
            String title = "가계부 초대";
            String body = String.format("%s님이 \"%s\" 가계부에 %s로 초대했습니다.", 
                inviterMember.getName(), book.getTitle(), roleText);
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction("open_book_detail")
                .userId(String.valueOf(inviterMember.getId()))
                .userNm(inviterMember.getName())
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(String.valueOf(invitedMember.getId()))
                .build();
            
            fcmSendService.sendPush(request);
            log.info("가계부 초대 알림 발송 완료. BookId: {}, InvitedUser: {}", bookId, invitedMember.getId());
            
        } catch (Exception e) {
            log.error("가계부 초대 알림 발송 실패", e);
        }
    }
    
    /**
     * 가계부 멤버 제거 알림
     * 
     * @param bookId 가계부 ID
     * @param removedMember 제거된 멤버
     * @param removerMember 제거한 멤버
     */
    public void sendMemberRemovedNotification(Long bookId, Member removedMember, Member removerMember) {
        try {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
            
            String title = "가계부 멤버 제거";
            String body = String.format("%s님이 \"%s\" 가계부에서 회원님을 제거했습니다.", 
                removerMember.getName(), book.getTitle());
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction("open_book_list")
                .userId(String.valueOf(removerMember.getId()))
                .userNm(removerMember.getName())
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(String.valueOf(removedMember.getId()))
                .build();
            
            fcmSendService.sendPush(request);
            log.info("멤버 제거 알림 발송 완료. BookId: {}, RemovedUser: {}", bookId, removedMember.getId());
            
        } catch (Exception e) {
            log.error("멤버 제거 알림 발송 실패", e);
        }
    }
    
    /**
     * 가계부 권한 변경 알림
     * 
     * @param bookId 가계부 ID
     * @param targetMember 권한이 변경된 멤버
     * @param changerMember 권한을 변경한 멤버
     * @param newRole 새로운 역할
     */
    public void sendRoleChangedNotification(Long bookId, Member targetMember, Member changerMember, String newRole) {
        try {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
            
            String roleText = "EDITOR".equals(newRole) ? "편집자" : "조회자";
            String title = "가계부 권한 변경";
            String body = String.format("%s님이 \"%s\" 가계부에서 회원님의 권한을 %s로 변경했습니다.", 
                changerMember.getName(), book.getTitle(), roleText);
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction("open_book_detail")
                .userId(String.valueOf(changerMember.getId()))
                .userNm(changerMember.getName())
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(String.valueOf(targetMember.getId()))
                .build();
            
            fcmSendService.sendPush(request);
            log.info("권한 변경 알림 발송 완료. BookId: {}, TargetUser: {}, NewRole: {}", bookId, targetMember.getId(), newRole);
            
        } catch (Exception e) {
            log.error("권한 변경 알림 발송 실패", e);
        }
    }
    
    /**
     * 가계부 멤버 탈퇴 알림 (다른 멤버들에게)
     * 
     * @param bookId 가계부 ID
     * @param leftMember 탈퇴한 멤버
     */
    public void sendMemberLeftNotification(Long bookId, Member leftMember) {
        try {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
            
            // 가계부 멤버들 조회 (탈퇴자 제외)
            List<UserBook> bookMembers = userBookJpaRepository.findByBookId(bookId)
                .stream()
                .filter(ub -> !ub.getMember().getId().equals(leftMember.getId()))
                .collect(Collectors.toList());
            
            if (bookMembers.isEmpty()) {
                log.info("알림 대상자가 없습니다. BookId: {}", bookId);
                return;
            }
            
            String userIds = bookMembers.stream()
                .map(ub -> String.valueOf(ub.getMember().getId()))
                .collect(Collectors.joining(","));
            
            String title = "가계부 멤버 탈퇴";
            String body = String.format("%s님이 \"%s\" 가계부에서 탈퇴했습니다.", 
                leftMember.getName(), book.getTitle());
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction("open_book_detail")
                .userId(String.valueOf(leftMember.getId()))
                .userNm(leftMember.getName())
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(userIds)
                .build();
            
            fcmSendService.sendPush(request);
            log.info("멤버 탈퇴 알림 발송 완료. BookId: {}, LeftUser: {}, UserCount: {}", bookId, leftMember.getId(), bookMembers.size());
            
        } catch (Exception e) {
            log.error("멤버 탈퇴 알림 발송 실패", e);
        }
    }
    
    /**
     * 친구 요청 알림
     * 
     * @param receiverId 수신자 ID
     * @param senderId 발신자 ID  
     * @param senderName 발신자 이름
     */
    public void sendFriendRequestNotification(Long receiverId, Long senderId, String senderName) {
        try {
            String title = "새로운 친구 요청";
            String body = String.format("%s님이 친구 요청을 보냈습니다.", senderName);
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction("open_friend_requests")
                .userId(String.valueOf(senderId))
                .userNm(senderName)
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(String.valueOf(receiverId))
                .build();
            
            fcmSendService.sendPush(request);
            log.info("친구 요청 알림 발송 완료. Receiver: {}, Sender: {}", receiverId, senderId);
            
        } catch (Exception e) {
            log.error("친구 요청 알림 발송 실패", e);
        }
    }
    
    /**
     * 친구 수락 알림
     * 
     * @param receiverId 수신자 ID
     * @param acceptorId 수락한 사용자 ID
     * @param acceptorName 수락한 사용자 이름
     */
    public void sendFriendAcceptNotification(Long receiverId, Long acceptorId, String acceptorName) {
        try {
            String title = "친구 요청 수락됨";
            String body = String.format("%s님이 친구 요청을 수락했습니다.", acceptorName);
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction("open_friends")
                .userId(String.valueOf(acceptorId))
                .userNm(acceptorName)
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(String.valueOf(receiverId))
                .build();
            
            fcmSendService.sendPush(request);
            log.info("친구 수락 알림 발송 완료. Receiver: {}, Acceptor: {}", receiverId, acceptorId);
            
        } catch (Exception e) {
            log.error("친구 수락 알림 발송 실패", e);
        }
    }
    
    /**
     * 예산 초과 알림
     * 
     * @param bookId 가계부 ID
     * @param currentAmount 현재 지출 금액
     * @param budgetAmount 예산 금액
     * @param percentage 사용률 (%)
     */
    public void sendBudgetExceededNotification(Long bookId, int currentAmount, int budgetAmount, int percentage) {
        try {
            Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
            
            // 가계부 멤버들 조회
            List<UserBook> bookMembers = userBookJpaRepository.findByBookId(bookId);
            
            if (bookMembers.isEmpty()) {
                log.info("알림 대상자가 없습니다. BookId: {}", bookId);
                return;
            }
            
            String userIds = bookMembers.stream()
                .map(ub -> String.valueOf(ub.getMember().getId()))
                .collect(Collectors.joining(","));
            
            String title = "예산 초과 알림";
            String body = String.format("\"%s\" 가계부의 이번 달 예산 %d%%를 사용했습니다. (₩%,d/₩%,d)", 
                book.getTitle(), percentage, currentAmount, budgetAmount);
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction("open_budget_detail")
                .userId("system")
                .userNm("시스템")
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(userIds)
                .build();
            
            fcmSendService.sendPush(request);
            log.info("예산 초과 알림 발송 완료. BookId: {}, Percentage: {}%, UserCount: {}", bookId, percentage, bookMembers.size());
            
        } catch (Exception e) {
            log.error("예산 초과 알림 발송 실패", e);
        }
    }
    
    /**
     * 일반 알림 전송
     * 
     * @param userIds 대상 사용자 ID 목록
     * @param title 알림 제목
     * @param body 알림 내용
     * @param action 알림 액션
     */
    public void sendGeneralNotification(List<Long> userIds, String title, String body, String action) {
        try {
            String userIdList = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction(action)
                .userId("system")
                .userNm("시스템")
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(userIdList)
                .build();
            
            fcmSendService.sendPush(request);
            log.info("일반 알림 발송 완료. UserCount: {}, Title: {}", userIds.size(), title);
            
        } catch (Exception e) {
            log.error("일반 알림 발송 실패", e);
        }
    }
    
    /**
     * 예약 알림 전송
     * 
     * @param userIds 대상 사용자 ID 목록
     * @param title 알림 제목
     * @param body 알림 내용
     * @param action 알림 액션
     * @param scheduledTime 예약 시간
     */
    public void sendScheduledNotification(List<Long> userIds, String title, String body, String action, LocalDateTime scheduledTime) {
        try {
            String userIdList = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId(APP_ID)
                .noticeTitle(title)
                .noticeBody(body)
                .noticeAction(action)
                .userId("system")
                .userNm("시스템")
                .pushUse("Y")
                .smsUse("N")
                .webUse("N")
                .userNmAt("N")
                .sendUserList(userIdList)
                .reservationDt(scheduledTime)
                .build();
            
            fcmSendService.sendPush(request);
            log.info("예약 알림 등록 완료. UserCount: {}, Title: {}, ScheduledTime: {}", userIds.size(), title, scheduledTime);
            
        } catch (Exception e) {
            log.error("예약 알림 등록 실패", e);
        }
    }
}