package garabu.garabuServer.service;

import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.repository.UserBookJpaRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.jwt.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookService {

    private final UserBookJpaRepository userBookJpaRepository;
    private final MemberJPARepository memberJPARepository;
    private final BookRepository bookRepository;
    private final BookSharingNotificationService notificationService;

    /** 가계부-회원 매핑 등록 */
    @Transactional
    public Long registUserBook(UserBook userBook) {
        userBookJpaRepository.save(userBook);
        return userBook.getId();
    }

    /** bookId에 해당하는 소유자(UserBook) 리스트 조회 */
    public List<UserBook> findOwnersByBookId(Long bookId) {

        /* 1) 가계부 존재 여부 검증 */
        if (!userBookJpaRepository.existsByBookId(bookId)) {
            throw new EntityNotFoundException("해당 ID(" + bookId + ")의 가계부를 찾을 수 없습니다.");
        }

        /* 2) 소유자 조회 (Member를 FETCH JOIN) */
        List<UserBook> owners = userBookJpaRepository.findByBookId(bookId);

        /* 3) 소유자가 없을 경우 비즈니스 예외 발생 */
        if (owners.isEmpty()) {
            throw new EntityNotFoundException("bookId=" + bookId + " 에 대한 소유자 정보가 없습니다.");
        }

        return owners;
    }
    
    /**
     * 가계부 ID와 회원 ID로 UserBook 조회
     */
    public Optional<UserBook> findByBookIdAndMemberId(Long bookId, Long memberId) {
        return userBookJpaRepository.findByBookIdAndMemberId(bookId, memberId);
    }
    
    /**
     * 현재 로그인한 사용자 정보 조회
     */
    private Member getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return memberJPARepository.findByUsername(userDetails.getUsername());
    }
    
    /**
     * 사용자가 가계부의 소유자인지 확인
     */
    private void validateOwnership(Long bookId, Member currentUser) {
        UserBook userBook = userBookJpaRepository.findByBookIdAndMemberId(bookId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 가계부에 대한 접근 권한이 없습니다."));
        
        if (!userBook.getBookRole().equals(BookRole.OWNER)) {
            throw new IllegalArgumentException("소유자만 이 작업을 수행할 수 있습니다.");
        }
    }
    
    /**
     * 가계부에 사용자 초대
     */
    @Transactional
    public void inviteUser(Long bookId, String email, BookRole role) {
        Member currentUser = getCurrentUser();
        validateOwnership(bookId, currentUser);
        
        // 초대할 사용자 조회
        Member invitedUser = memberJPARepository.findOneByEmail(email);
        if (invitedUser == null) {
            throw new EntityNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email);
        }
        
        // 이미 참여 중인지 확인
        if (userBookJpaRepository.existsByBookIdAndMemberId(bookId, invitedUser.getId())) {
            throw new IllegalArgumentException("이미 가계부에 참여 중인 사용자입니다.");
        }
        
        // 가계부 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("해당 가계부를 찾을 수 없습니다."));
        
        // OWNER 역할로 초대 불가
        if (role == BookRole.OWNER) {
            throw new IllegalArgumentException("소유자 역할로는 초대할 수 없습니다.");
        }
        
        // UserBook 생성 및 저장
        UserBook userBook = new UserBook();
        userBook.setMember(invitedUser);
        userBook.setBook(book);
        userBook.setBookRole(role);
        
        userBookJpaRepository.save(userBook);
        
        // 초대 알림 발송
        notificationService.sendBookInvitationNotification(invitedUser, book, currentUser.getName(), role);
    }
    
    /**
     * 가계부에서 멤버 제거
     */
    @Transactional
    public void removeMember(Long bookId, Long memberId) {
        Member currentUser = getCurrentUser();
        validateOwnership(bookId, currentUser);
        
        // 제거할 멤버 조회
        UserBook userBook = userBookJpaRepository.findByBookIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 가계부에 참여하지 않는 사용자입니다."));
        
        // 소유자 자신은 제거할 수 없음
        if (userBook.getBookRole() == BookRole.OWNER) {
            throw new IllegalArgumentException("소유자는 제거할 수 없습니다.");
        }
        
        // 멤버 제거 알림 발송 (제거되기 전에 발송)
        notificationService.sendMemberRemovedNotification(userBook.getMember(), userBook.getBook(), currentUser.getName());
        
        userBookJpaRepository.deleteByBookIdAndMemberId(bookId, memberId);
    }
    
    /**
     * 멤버의 역할 변경
     */
    @Transactional
    public void changeRole(Long bookId, Long memberId, BookRole newRole) {
        Member currentUser = getCurrentUser();
        validateOwnership(bookId, currentUser);
        
        // 역할을 변경할 멤버 조회
        UserBook userBook = userBookJpaRepository.findByBookIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("해당 가계부에 참여하지 않는 사용자입니다."));
        
        // 소유자의 역할은 변경할 수 없음
        if (userBook.getBookRole() == BookRole.OWNER) {
            throw new IllegalArgumentException("소유자의 역할은 변경할 수 없습니다.");
        }
        
        // OWNER 역할로 변경 불가
        if (newRole == BookRole.OWNER) {
            throw new IllegalArgumentException("소유자 역할로는 변경할 수 없습니다.");
        }
        
        userBook.setBookRole(newRole);
        userBookJpaRepository.save(userBook);
        
        // 권한 변경 알림 발송
        notificationService.sendRoleChangedNotification(userBook.getMember(), userBook.getBook(), newRole, currentUser.getName());
    }
    
    /**
     * 가계부 나가기
     */
    @Transactional
    public void leaveBook(Long bookId) {
        Member currentUser = getCurrentUser();
        
        // 현재 사용자의 가계부 참여 정보 조회
        UserBook userBook = userBookJpaRepository.findByBookIdAndMemberId(bookId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("해당 가계부에 참여하지 않습니다."));
        
        // 소유자는 나갈 수 없음
        if (userBook.getBookRole() == BookRole.OWNER) {
            throw new IllegalArgumentException("소유자는 가계부를 나갈 수 없습니다.");
        }
        
        // 멤버 탈퇴 알림 발송 (탈퇴하기 전에 발송)
        notificationService.sendMemberLeftNotification(currentUser, userBook.getBook());
        
        userBookJpaRepository.deleteByBookIdAndMemberId(bookId, currentUser.getId());
    }
}
