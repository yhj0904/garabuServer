package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.exception.BookAccessException;
import garabu.garabuServer.exception.InsufficientPermissionException;
import garabu.garabuServer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 가계부 초대 및 참가 요청 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookInviteService {
    
    private final UserBookRequestRepository requestRepository;
    private final UserBookGroupRepository groupRepository;
    private final UserBookJpaRepository userBookRepository;
    private final BookRepository bookRepository;
    private final MemberService memberService;
    private final InviteCodeService inviteCodeService;
    private final BookSharingNotificationService notificationService;
    private final BookService bookService;
    
    /**
     * 가계부 초대 코드 생성 (OWNER만 가능)
     */
    @Transactional
    public String createBookInviteCode(Long bookId, BookRole role) {
        Member currentUser = memberService.getCurrentMember();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        // OWNER 권한 확인
        validateOwnership(book, currentUser);
        
        // OWNER 역할로는 초대 불가
        if (role == BookRole.OWNER) {
            throw new IllegalArgumentException("OWNER 권한으로는 초대할 수 없습니다.");
        }
        
        return inviteCodeService.generateBookInviteCode(bookId, role.name());
    }
    
    /**
     * 사용자 식별 코드 생성
     */
    @Transactional
    public String createUserIdCode() {
        Member currentUser = memberService.getCurrentMember();
        return inviteCodeService.generateUserIdCode(currentUser.getId());
    }
    
    /**
     * 초대 코드로 가계부 참가 요청
     */
    @Transactional
    public UserBookRequest requestJoinBook(String inviteCode) {
        Member currentUser = memberService.getCurrentMember();
        
        // 초대 코드 확인
        InviteCodeService.BookInviteData inviteData = inviteCodeService.getBookInviteData(inviteCode);
        if (inviteData == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 초대 코드입니다.");
        }
        
        Book book = bookRepository.findById(inviteData.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        // 이미 가계부 멤버인지 확인
        if (userBookRepository.existsByBookIdAndMemberId(book.getId(), currentUser.getId())) {
            throw new IllegalArgumentException("이미 가계부에 참여하고 있습니다.");
        }
        
        // 이미 대기중인 요청이 있는지 확인
        if (requestRepository.existsByBookAndMemberAndStatus(book, currentUser, RequestStatus.PENDING)) {
            throw new IllegalArgumentException("이미 참가 요청이 대기 중입니다.");
        }
        
        // 참가 요청 생성
        UserBookRequest request = UserBookRequest.builder()
                .book(book)
                .member(currentUser)
                .status(RequestStatus.PENDING)
                .requestedRole(BookRole.valueOf(inviteData.getRole()))
                .inviteCode(inviteCode)
                .build();
        
        UserBookRequest savedRequest = requestRepository.save(request);
        
        // 가계부 소유자에게 알림
        Member owner = book.getOwner();
        notificationService.sendJoinRequestNotification(owner, book, currentUser.getUsername());
        
        log.info("가계부 참가 요청 생성 - bookId: {}, memberId: {}, role: {}", 
                book.getId(), currentUser.getId(), inviteData.getRole());
        
        return savedRequest;
    }
    
    /**
     * 참가 요청 수락 (OWNER만 가능)
     */
    @Transactional
    public void acceptJoinRequest(Long requestId) {
        Member currentUser = memberService.getCurrentMember();
        UserBookRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));
        
        // OWNER 권한 확인
        validateOwnership(request.getBook(), currentUser);
        
        // 이미 처리된 요청인지 확인
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }
        
        // UserBook 생성
        UserBook userBook = new UserBook();
        userBook.setBook(request.getBook());
        userBook.setMember(request.getMember());
        userBook.setBookRole(request.getRequestedRole());
        userBookRepository.save(userBook);
        
        // 캐시 무효화 - 가계부 목록 캐시 초기화
        bookService.clearUserBooksCache();
        
        // 요청 상태 업데이트
        request.setStatus(RequestStatus.ACCEPTED);
        request.setResponseDate(LocalDateTime.now());
        request.setRespondedBy(currentUser);
        
        // 요청자에게 알림
        notificationService.sendJoinAcceptedNotification(
                request.getMember(), request.getBook(), request.getRequestedRole());
        
        log.info("가계부 참가 요청 수락 - requestId: {}, bookId: {}, memberId: {}", 
                requestId, request.getBook().getId(), request.getMember().getId());
    }
    
    /**
     * 참가 요청 거절 (OWNER만 가능)
     */
    @Transactional
    public void rejectJoinRequest(Long requestId) {
        Member currentUser = memberService.getCurrentMember();
        UserBookRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청을 찾을 수 없습니다."));
        
        // OWNER 권한 확인
        validateOwnership(request.getBook(), currentUser);
        
        // 이미 처리된 요청인지 확인
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 요청입니다.");
        }
        
        // 요청 상태 업데이트
        request.setStatus(RequestStatus.REJECTED);
        request.setResponseDate(LocalDateTime.now());
        request.setRespondedBy(currentUser);
        
        // 요청자에게 알림
        notificationService.sendJoinRejectedNotification(request.getMember(), request.getBook());
        
        log.info("가계부 참가 요청 거절 - requestId: {}, bookId: {}, memberId: {}", 
                requestId, request.getBook().getId(), request.getMember().getId());
    }
    
    /**
     * 가계부의 참가 요청 목록 조회 (OWNER만 가능)
     */
    public List<UserBookRequest> getBookJoinRequests(Long bookId) {
        Member currentUser = memberService.getCurrentMember();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        // OWNER 권한 확인
        validateOwnership(book, currentUser);
        
        return requestRepository.findByBookOrderByStatusAndRequestDate(book);
    }
    
    /**
     * 내가 요청한 가계부 목록 조회
     */
    public List<UserBookRequest> getMyJoinRequests() {
        Member currentUser = memberService.getCurrentMember();
        return requestRepository.findByMemberOrderByRequestDateDesc(currentUser);
    }
    
    /**
     * 그룹 생성 (OWNER만 가능)
     */
    @Transactional
    public UserBookGroup createGroup(Long bookId, String groupName, String description) {
        Member currentUser = memberService.getCurrentMember();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        // OWNER 권한 확인
        validateOwnership(book, currentUser);
        
        // 그룹명 중복 확인
        if (groupRepository.existsByBookAndGroupName(book, groupName)) {
            throw new IllegalArgumentException("이미 존재하는 그룹명입니다.");
        }
        
        UserBookGroup group = UserBookGroup.builder()
                .book(book)
                .groupName(groupName)
                .description(description)
                .createdBy(currentUser)
                .build();
        
        return groupRepository.save(group);
    }
    
    /**
     * 그룹에 멤버 추가 (OWNER만 가능)
     */
    @Transactional
    public void addMemberToGroup(Long groupId, Long userBookId) {
        Member currentUser = memberService.getCurrentMember();
        UserBookGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        
        // OWNER 권한 확인
        validateOwnership(group.getBook(), currentUser);
        
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부 멤버를 찾을 수 없습니다."));
        
        // 같은 가계부인지 확인
        if (!userBook.getBook().getId().equals(group.getBook().getId())) {
            throw new IllegalArgumentException("다른 가계부의 멤버는 추가할 수 없습니다.");
        }
        
        // 이미 그룹에 속해있는지 확인
        boolean alreadyInGroup = group.getGroupMembers().stream()
                .anyMatch(member -> member.getUserBook().getId().equals(userBookId));
        
        if (alreadyInGroup) {
            throw new IllegalArgumentException("이미 그룹에 속해있는 멤버입니다.");
        }
        
        UserBookGroupMember groupMember = UserBookGroupMember.builder()
                .group(group)
                .userBook(userBook)
                .build();
        
        group.getGroupMembers().add(groupMember);
        
        log.info("그룹에 멤버 추가 - groupId: {}, userBookId: {}", groupId, userBookId);
    }
    
    /**
     * 가계부의 그룹 목록 조회
     */
    public List<UserBookGroup> getBookGroups(Long bookId) {
        Member currentUser = memberService.getCurrentMember();
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        // 가계부 접근 권한 확인
        if (!userBookRepository.existsByBookIdAndMemberId(bookId, currentUser.getId())) {
            throw new BookAccessException("가계부에 접근 권한이 없습니다.");
        }
        
        return groupRepository.findByBookOrderByGroupName(book);
    }
    
    /**
     * OWNER 권한 검증
     */
    private void validateOwnership(Book book, Member member) {
        UserBook userBook = userBookRepository.findByBookIdAndMemberId(book.getId(), member.getId())
                .orElseThrow(() -> new BookAccessException("가계부에 접근 권한이 없습니다."));
        
        if (userBook.getBookRole() != BookRole.OWNER) {
            throw new InsufficientPermissionException("소유자 권한이 필요합니다.");
        }
    }
} 