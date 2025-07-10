package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.dto.FcmSendRequestDTO;
import garabu.garabuServer.repository.UserBookJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 가계부 공유 관련 알림 서비스
 * 
 * 가계부 공유 이벤트(초대, 멤버 추가/제거, 권한 변경 등)에 대한 
 * 실시간 푸시 알림을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookSharingNotificationService {
    
    private final FcmSendService fcmSendService;
    private final UserBookJpaRepository userBookJpaRepository;
    
    private static final String APP_ID = "garabu";
    
    /**
     * 가계부 초대 알림 발송
     * 
     * @param invitedUser 초대받은 사용자
     * @param book 가계부 정보
     * @param inviterName 초대한 사용자 이름
     * @param role 부여된 역할
     */
    public void sendBookInvitationNotification(Member invitedUser, Book book, String inviterName, BookRole role) {
        try {
            String title = "가계부 초대 알림";
            String body = String.format("%s님이 '%s' 가계부에 %s로 초대했습니다.", 
                    inviterName, book.getTitle(), getRoleDisplayName(role));
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                    .appId(APP_ID)
                    .noticeTitle(title)
                    .noticeBody(body)
                    .noticeAction("BOOK_INVITATION")
                    .noticeUrl("/book/" + book.getId())
                    .userId(invitedUser.getId().toString())
                    .userNm(invitedUser.getName())
                    .sendUserList(invitedUser.getId().toString())
                    .pushUse("Y")
                    .smsUse("N")
                    .webUse("N")
                    .userNmAt("Y")
                    .build();
            
            fcmSendService.sendPush(request);
            log.info("가계부 초대 알림 발송 완료 - 대상: {}, 가계부: {}", invitedUser.getEmail(), book.getTitle());
            
        } catch (Exception e) {
            log.error("가계부 초대 알림 발송 실패 - 대상: {}, 가계부: {}, 오류: {}", 
                    invitedUser.getEmail(), book.getTitle(), e.getMessage());
        }
    }
    
    /**
     * 가계부 멤버 제거 알림 발송
     * 
     * @param removedUser 제거된 사용자
     * @param book 가계부 정보
     * @param removerName 제거한 사용자 이름
     */
    public void sendMemberRemovedNotification(Member removedUser, Book book, String removerName) {
        try {
            String title = "가계부 멤버 제거 알림";
            String body = String.format("%s님이 '%s' 가계부에서 회원님을 제거했습니다.", 
                    removerName, book.getTitle());
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                    .appId(APP_ID)
                    .noticeTitle(title)
                    .noticeBody(body)
                    .noticeAction("MEMBER_REMOVED")
                    .noticeUrl("/book/" + book.getId())
                    .userId(removedUser.getId().toString())
                    .userNm(removedUser.getName())
                    .sendUserList(removedUser.getId().toString())
                    .pushUse("Y")
                    .smsUse("N")
                    .webUse("N")
                    .userNmAt("Y")
                    .build();
            
            fcmSendService.sendPush(request);
            log.info("가계부 멤버 제거 알림 발송 완료 - 대상: {}, 가계부: {}", removedUser.getEmail(), book.getTitle());
            
        } catch (Exception e) {
            log.error("가계부 멤버 제거 알림 발송 실패 - 대상: {}, 가계부: {}, 오류: {}", 
                    removedUser.getEmail(), book.getTitle(), e.getMessage());
        }
    }
    
    /**
     * 가계부 권한 변경 알림 발송
     * 
     * @param targetUser 권한이 변경된 사용자
     * @param book 가계부 정보
     * @param newRole 새로운 역할
     * @param changerName 권한을 변경한 사용자 이름
     */
    public void sendRoleChangedNotification(Member targetUser, Book book, BookRole newRole, String changerName) {
        try {
            String title = "가계부 권한 변경 알림";
            String body = String.format("%s님이 '%s' 가계부에서 회원님의 권한을 %s로 변경했습니다.", 
                    changerName, book.getTitle(), getRoleDisplayName(newRole));
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                    .appId(APP_ID)
                    .noticeTitle(title)
                    .noticeBody(body)
                    .noticeAction("ROLE_CHANGED")
                    .noticeUrl("/book/" + book.getId())
                    .userId(targetUser.getId().toString())
                    .userNm(targetUser.getName())
                    .sendUserList(targetUser.getId().toString())
                    .pushUse("Y")
                    .smsUse("N")
                    .webUse("N")
                    .userNmAt("Y")
                    .build();
            
            fcmSendService.sendPush(request);
            log.info("가계부 권한 변경 알림 발송 완료 - 대상: {}, 가계부: {}, 새 권한: {}", 
                    targetUser.getEmail(), book.getTitle(), newRole);
            
        } catch (Exception e) {
            log.error("가계부 권한 변경 알림 발송 실패 - 대상: {}, 가계부: {}, 오류: {}", 
                    targetUser.getEmail(), book.getTitle(), e.getMessage());
        }
    }
    
    /**
     * 가계부 멤버 탈퇴 알림 발송 (다른 멤버들에게)
     * 
     * @param leavingUser 탈퇴한 사용자
     * @param book 가계부 정보
     */
    public void sendMemberLeftNotification(Member leavingUser, Book book) {
        try {
            List<UserBook> remainingMembers = userBookJpaRepository.findByBookId(book.getId());
            
            if (remainingMembers.isEmpty()) {
                return;
            }
            
            String userIds = remainingMembers.stream()
                    .map(ub -> ub.getMember().getId().toString())
                    .collect(Collectors.joining(","));
            
            String title = "가계부 멤버 탈퇴 알림";
            String body = String.format("%s님이 '%s' 가계부에서 탈퇴했습니다.", 
                    leavingUser.getName(), book.getTitle());
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                    .appId(APP_ID)
                    .noticeTitle(title)
                    .noticeBody(body)
                    .noticeAction("MEMBER_LEFT")
                    .noticeUrl("/book/" + book.getId())
                    .userId(leavingUser.getId().toString())
                    .userNm(leavingUser.getName())
                    .sendUserList(userIds)
                    .pushUse("Y")
                    .smsUse("N")
                    .webUse("N")
                    .userNmAt("Y")
                    .build();
            
            fcmSendService.sendPush(request);
            log.info("가계부 멤버 탈퇴 알림 발송 완료 - 탈퇴자: {}, 가계부: {}, 알림 대상: {}명", 
                    leavingUser.getEmail(), book.getTitle(), remainingMembers.size());
            
        } catch (Exception e) {
            log.error("가계부 멤버 탈퇴 알림 발송 실패 - 탈퇴자: {}, 가계부: {}, 오류: {}", 
                    leavingUser.getEmail(), book.getTitle(), e.getMessage());
        }
    }
    
    /**
     * 가계부 새 기록 추가 알림 발송 (공유 가계부의 다른 멤버들에게)
     * 
     * @param book 가계부 정보
     * @param authorName 기록을 작성한 사용자 이름
     * @param authorId 기록을 작성한 사용자 ID
     * @param ledgerDescription 기록 설명
     * @param amount 금액
     */
    public void sendNewLedgerEntryNotification(Book book, String authorName, Long authorId, 
                                             String ledgerDescription, Integer amount) {
        try {
            List<UserBook> members = userBookJpaRepository.findByBookId(book.getId());
            
            // 작성자 본인은 제외
            List<UserBook> otherMembers = members.stream()
                    .filter(ub -> !ub.getMember().getId().equals(authorId))
                    .collect(Collectors.toList());
            
            if (otherMembers.isEmpty()) {
                return;
            }
            
            String userIds = otherMembers.stream()
                    .map(ub -> ub.getMember().getId().toString())
                    .collect(Collectors.joining(","));
            
            String title = "가계부 새 기록 알림";
            String body = String.format("%s님이 '%s' 가계부에 새 기록을 추가했습니다: %s (%,d원)", 
                    authorName, book.getTitle(), ledgerDescription, amount);
            
            FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                    .appId(APP_ID)
                    .noticeTitle(title)
                    .noticeBody(body)
                    .noticeAction("NEW_LEDGER_ENTRY")
                    .noticeUrl("/book/" + book.getId())
                    .userId(authorId.toString())
                    .userNm(authorName)
                    .sendUserList(userIds)
                    .pushUse("Y")
                    .smsUse("N")
                    .webUse("N")
                    .userNmAt("Y")
                    .build();
            
            fcmSendService.sendPush(request);
            log.info("가계부 새 기록 알림 발송 완료 - 작성자: {}, 가계부: {}, 알림 대상: {}명", 
                    authorName, book.getTitle(), otherMembers.size());
            
        } catch (Exception e) {
            log.error("가계부 새 기록 알림 발송 실패 - 작성자: {}, 가계부: {}, 오류: {}", 
                    authorName, book.getTitle(), e.getMessage());
        }
    }
    
    /**
     * 역할 표시명 반환
     */
    private String getRoleDisplayName(BookRole role) {
        switch (role) {
            case OWNER:
                return "소유자";
            case EDITOR:
                return "편집자";
            case VIEWER:
                return "조회자";
            default:
                return "멤버";
        }
    }
}