package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import garabu.garabuServer.jwt.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserBookServiceTest {

    @Mock
    private UserBookJpaRepository userBookJpaRepository;
    
    @Mock
    private MemberJPARepository memberJPARepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private BookSharingNotificationService notificationService;
    
    @InjectMocks
    private UserBookService userBookService;
    
    private Member owner;
    private Member invitedUser;
    private Book book;
    private UserBook ownerUserBook;
    
    @BeforeEach
    void setUp() {
        // 테스트용 멤버 생성
        owner = new Member();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setEmail("owner@example.com");
        owner.setName("소유자");
        
        invitedUser = new Member();
        invitedUser.setId(2L);
        invitedUser.setUsername("invited");
        invitedUser.setEmail("invited@example.com");
        invitedUser.setName("초대받은사용자");
        
        // 테스트용 가계부 생성
        book = new Book();
        book.setId(1L);
        book.setTitle("테스트 가계부");
        book.setOwner(owner);
        
        // 소유자 UserBook 생성
        ownerUserBook = new UserBook();
        ownerUserBook.setId(1L);
        ownerUserBook.setMember(owner);
        ownerUserBook.setBook(book);
        ownerUserBook.setBookRole(BookRole.OWNER);
        
        // SecurityContext 설정
        mockSecurityContext();
    }
    
    private void mockSecurityContext() {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        lenient().when(userDetails.getUsername()).thenReturn("owner");
        
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        
        SecurityContextHolder.setContext(securityContext);
    }
    
    @Test
    @DisplayName("가계부 소유자 목록 조회 - 성공")
    void findOwnersByBookId_Success() {
        // given
        Long bookId = 1L;
        List<UserBook> userBooks = Arrays.asList(ownerUserBook);
        
        when(userBookJpaRepository.existsByBookId(bookId)).thenReturn(true);
        when(userBookJpaRepository.findByBookId(bookId)).thenReturn(userBooks);
        
        // when
        List<UserBook> result = userBookService.findOwnersByBookId(bookId);
        
        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(owner.getId(), result.get(0).getMember().getId());
        verify(userBookJpaRepository).existsByBookId(bookId);
        verify(userBookJpaRepository).findByBookId(bookId);
    }
    
    @Test
    @DisplayName("가계부 소유자 목록 조회 - 가계부 없음")
    void findOwnersByBookId_BookNotFound() {
        // given
        Long bookId = 999L;
        
        when(userBookJpaRepository.existsByBookId(bookId)).thenReturn(false);
        
        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            userBookService.findOwnersByBookId(bookId);
        });
        
        verify(userBookJpaRepository).existsByBookId(bookId);
        verify(userBookJpaRepository, never()).findByBookId(anyLong());
    }
    
    @Test
    @DisplayName("사용자 초대 - 성공")
    void inviteUser_Success() {
        // given
        Long bookId = 1L;
        String email = "invited@example.com";
        BookRole role = BookRole.EDITOR;
        
        when(memberJPARepository.findByUsername("owner")).thenReturn(owner);
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, owner.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        when(memberJPARepository.findOneByEmail(email)).thenReturn(invitedUser);
        when(userBookJpaRepository.existsByBookIdAndMemberId(bookId, invitedUser.getId()))
                .thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        
        // when
        userBookService.inviteUser(bookId, email, role);
        
        // then
        verify(userBookJpaRepository).save(any(UserBook.class));
        verify(notificationService).sendBookInvitationNotification(invitedUser, book, owner.getName(), role);
    }
    
    @Test
    @DisplayName("사용자 초대 - 권한 없음 (소유자가 아님)")
    void inviteUser_NotOwner() {
        // given
        Long bookId = 1L;
        String email = "invited@example.com";
        BookRole role = BookRole.EDITOR;
        
        UserBook editorUserBook = new UserBook();
        editorUserBook.setBookRole(BookRole.EDITOR);
        
        when(memberJPARepository.findByUsername("owner")).thenReturn(owner);
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, owner.getId()))
                .thenReturn(Optional.of(editorUserBook));
        
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userBookService.inviteUser(bookId, email, role);
        });
        
        verify(userBookJpaRepository, never()).save(any(UserBook.class));
        verify(notificationService, never()).sendBookInvitationNotification(any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("사용자 초대 - 이미 참여 중인 사용자")
    void inviteUser_AlreadyMember() {
        // given
        Long bookId = 1L;
        String email = "invited@example.com";
        BookRole role = BookRole.EDITOR;
        
        when(memberJPARepository.findByUsername("owner")).thenReturn(owner);
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, owner.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        when(memberJPARepository.findOneByEmail(email)).thenReturn(invitedUser);
        when(userBookJpaRepository.existsByBookIdAndMemberId(bookId, invitedUser.getId()))
                .thenReturn(true);
        
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userBookService.inviteUser(bookId, email, role);
        });
        
        verify(userBookJpaRepository, never()).save(any(UserBook.class));
        verify(notificationService, never()).sendBookInvitationNotification(any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("사용자 초대 - OWNER 역할로 초대 불가")
    void inviteUser_CannotInviteAsOwner() {
        // given
        Long bookId = 1L;
        String email = "invited@example.com";
        BookRole role = BookRole.OWNER;
        
        when(memberJPARepository.findByUsername("owner")).thenReturn(owner);
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, owner.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        when(memberJPARepository.findOneByEmail(email)).thenReturn(invitedUser);
        when(userBookJpaRepository.existsByBookIdAndMemberId(bookId, invitedUser.getId()))
                .thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userBookService.inviteUser(bookId, email, role);
        });
        
        verify(userBookJpaRepository, never()).save(any(UserBook.class));
        verify(notificationService, never()).sendBookInvitationNotification(any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("멤버 제거 - 성공")
    void removeMember_Success() {
        // given
        Long bookId = 1L;
        Long memberId = 2L;
        
        UserBook memberUserBook = new UserBook();
        memberUserBook.setMember(invitedUser);
        memberUserBook.setBook(book);
        memberUserBook.setBookRole(BookRole.EDITOR);
        
        when(memberJPARepository.findByUsername("owner")).thenReturn(owner);
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, owner.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, memberId))
                .thenReturn(Optional.of(memberUserBook));
        
        // when
        userBookService.removeMember(bookId, memberId);
        
        // then
        verify(notificationService).sendMemberRemovedNotification(invitedUser, book, owner.getName());
        verify(userBookJpaRepository).deleteByBookIdAndMemberId(bookId, memberId);
    }
    
    @Test
    @DisplayName("멤버 제거 - 소유자는 제거할 수 없음")
    void removeMember_CannotRemoveOwner() {
        // given
        Long bookId = 1L;
        Long memberId = 1L; // 소유자 ID
        
        when(memberJPARepository.findByUsername("owner")).thenReturn(owner);
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, owner.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        when(userBookJpaRepository.findByBookIdAndMemberId(bookId, memberId))
                .thenReturn(Optional.of(ownerUserBook));
        
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userBookService.removeMember(bookId, memberId);
        });
        
        verify(notificationService, never()).sendMemberRemovedNotification(any(), any(), any());
        verify(userBookJpaRepository, never()).deleteByBookIdAndMemberId(anyLong(), anyLong());
    }
}