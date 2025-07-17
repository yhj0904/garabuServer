package garabu.garabuServer.service;

import com.niamedtech.expo.exposerversdk.response.TicketResponse;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Expo 푸시 알림 서비스
 * 
 * 가계부 앱의 다양한 상황에 맞는 Expo 푸시 알림을 전송합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpoPushNotificationService {
    
    private final ExpoNotificationService expoNotificationService;
    private final ExpoTokenService expoTokenService;
    private final BookRepository bookRepository;
    private final UserBookJpaRepository userBookJpaRepository;
    
    /**
     * 새 거래 내역 추가 알림
     * 
     * @param ledger 새로 추가된 거래 내역
     * @param author 거래를 추가한 사용자
     */
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
            
            List<Long> memberIds = bookMembers.stream()
                .map(ub -> ub.getMember().getId())
                .collect(Collectors.toList());
            
            List<String> expoTokens = expoTokenService.getActiveTokensByUserIds(memberIds);
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. BookId: {}", ledger.getBook().getId());
                return;
            }
            
            String transactionType = ledger.getAmountType() == garabu.garabuServer.domain.AmountType.INCOME ? "수입" : "지출";
            String title = String.format("새 %s 추가", transactionType);
            String body = String.format("%s님이 \"%s\" 가계부에 새 %s을 추가했습니다: %s (₩%,d)", 
                author.getName(), book.getTitle(), transactionType, ledger.getDescription(), ledger.getAmount());
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_transaction_detail");
            data.put("ledgerId", ledger.getId());
            data.put("bookId", ledger.getBook().getId());
            data.put("url", String.format("garabuapp2://ledger/%d?bookId=%d", ledger.getId(), ledger.getBook().getId()));
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                List<String> receiptIds = tickets.stream()
                    .filter(ticket -> ticket.getId() != null)
                    .map(TicketResponse.Ticket::getId)
                    .collect(Collectors.toList());
                
                if (!receiptIds.isEmpty()) {
                    // 나중에 receipt 확인을 위해 저장하거나 스케줄링
                    expoNotificationService.checkReceipts(receiptIds);
                }
                
                log.info("새 거래 알림 발송 완료. BookId: {}, TokenCount: {}", ledger.getBook().getId(), expoTokens.size());
            });
            
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
            
            List<String> expoTokens = expoTokenService.getActiveTokensByUserId(invitedMember.getId());
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. UserId: {}", invitedMember.getId());
                return;
            }
            
            String roleText = "EDITOR".equals(role) ? "편집자" : "조회자";
            String title = "가계부 초대";
            String body = String.format("%s님이 \"%s\" 가계부에 %s로 초대했습니다.", 
                inviterMember.getName(), book.getTitle(), roleText);
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_book_detail");
            data.put("bookId", bookId);
            data.put("url", String.format("garabuapp2://book/%d", bookId));
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("가계부 초대 알림 발송 완료. BookId: {}, InvitedUser: {}", bookId, invitedMember.getId());
            });
            
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
            
            List<String> expoTokens = expoTokenService.getActiveTokensByUserId(removedMember.getId());
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. UserId: {}", removedMember.getId());
                return;
            }
            
            String title = "가계부 멤버 제거";
            String body = String.format("%s님이 \"%s\" 가계부에서 회원님을 제거했습니다.", 
                removerMember.getName(), book.getTitle());
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_book_list");
            data.put("url", "garabuapp2://books");
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("멤버 제거 알림 발송 완료. BookId: {}, RemovedUser: {}", bookId, removedMember.getId());
            });
            
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
            
            List<String> expoTokens = expoTokenService.getActiveTokensByUserId(targetMember.getId());
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. UserId: {}", targetMember.getId());
                return;
            }
            
            String roleText = "EDITOR".equals(newRole) ? "편집자" : "조회자";
            String title = "가계부 권한 변경";
            String body = String.format("%s님이 \"%s\" 가계부에서 회원님의 권한을 %s로 변경했습니다.", 
                changerMember.getName(), book.getTitle(), roleText);
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_book_detail");
            data.put("bookId", bookId);
            data.put("url", String.format("garabuapp2://book/%d", bookId));
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("권한 변경 알림 발송 완료. BookId: {}, TargetUser: {}, NewRole: {}", bookId, targetMember.getId(), newRole);
            });
            
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
            
            List<Long> memberIds = bookMembers.stream()
                .map(ub -> ub.getMember().getId())
                .collect(Collectors.toList());
            
            List<String> expoTokens = expoTokenService.getActiveTokensByUserIds(memberIds);
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. BookId: {}", bookId);
                return;
            }
            
            String title = "가계부 멤버 탈퇴";
            String body = String.format("%s님이 \"%s\" 가계부에서 탈퇴했습니다.", 
                leftMember.getName(), book.getTitle());
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_book_detail");
            data.put("bookId", bookId);
            data.put("url", String.format("garabuapp2://book/%d", bookId));
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("멤버 탈퇴 알림 발송 완료. BookId: {}, LeftUser: {}, TokenCount: {}", 
                    bookId, leftMember.getId(), expoTokens.size());
            });
            
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
            List<String> expoTokens = expoTokenService.getActiveTokensByUserId(receiverId);
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. UserId: {}", receiverId);
                return;
            }
            
            String title = "새로운 친구 요청";
            String body = String.format("%s님이 친구 요청을 보냈습니다.", senderName);
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_friend_requests");
            data.put("senderId", senderId);
            data.put("url", "garabuapp2://friends/requests");
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("친구 요청 알림 발송 완료. Receiver: {}, Sender: {}", receiverId, senderId);
            });
            
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
            List<String> expoTokens = expoTokenService.getActiveTokensByUserId(receiverId);
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. UserId: {}", receiverId);
                return;
            }
            
            String title = "친구 요청 수락됨";
            String body = String.format("%s님이 친구 요청을 수락했습니다.", acceptorName);
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_friends");
            data.put("acceptorId", acceptorId);
            data.put("url", "garabuapp2://friends");
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("친구 수락 알림 발송 완료. Receiver: {}, Acceptor: {}", receiverId, acceptorId);
            });
            
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
            
            List<Long> memberIds = bookMembers.stream()
                .map(ub -> ub.getMember().getId())
                .collect(Collectors.toList());
            
            List<String> expoTokens = expoTokenService.getActiveTokensByUserIds(memberIds);
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. BookId: {}", bookId);
                return;
            }
            
            String title = "예산 초과 알림";
            String body = String.format("\"%s\" 가계부의 이번 달 예산 %d%%를 사용했습니다. (₩%,d/₩%,d)", 
                book.getTitle(), percentage, currentAmount, budgetAmount);
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", "open_budget_detail");
            data.put("bookId", bookId);
            data.put("url", String.format("garabuapp2://book/%d/budget", bookId));
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("예산 초과 알림 발송 완료. BookId: {}, Percentage: {}%, TokenCount: {}", 
                    bookId, percentage, expoTokens.size());
            });
            
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
            List<String> expoTokens = expoTokenService.getActiveTokensByUserIds(userIds);
            
            if (expoTokens.isEmpty()) {
                log.info("활성화된 Expo 토큰이 없습니다. UserCount: {}", userIds.size());
                return;
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("action", action);
            data.put("url", "garabuapp2://home");
            
            CompletableFuture<List<TicketResponse.Ticket>> future = 
                expoNotificationService.sendPushNotifications(expoTokens, title, body, data);
            
            future.thenAccept(tickets -> {
                log.info("일반 알림 발송 완료. TokenCount: {}, Title: {}", expoTokens.size(), title);
            });
            
        } catch (Exception e) {
            log.error("일반 알림 발송 실패", e);
        }
    }
}