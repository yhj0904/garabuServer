package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.exception.BookAccessException;
import garabu.garabuServer.exception.InsufficientPermissionException;
import garabu.garabuServer.repository.CategoryJpaRepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * CategoryService 권한 검증 테스트
 */
@ExtendWith(MockitoExtension.class)
class CategoryServicePermissionTest {

    @Mock
    private CategoryJpaRepository categoryJpaRepository;
    
    @Mock
    private MemberService memberService;
    
    @Mock
    private UserBookJpaRepository userBookJpaRepository;
    
    @InjectMocks
    private CategoryService categoryService;
    
    private Member testMember;
    private Book testBook;
    private UserBook ownerUserBook;
    private UserBook editorUserBook;
    private UserBook viewerUserBook;
    
    @BeforeEach
    void setUp() {
        // 테스트 멤버 생성
        testMember = new Member();
        testMember.setId(1L);
        testMember.setUsername("testuser");
        testMember.setEmail("test@example.com");
        
        // 테스트 가계부 생성
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("테스트 가계부");
        
        // 권한별 UserBook 생성
        ownerUserBook = new UserBook();
        ownerUserBook.setId(1L);
        ownerUserBook.setMember(testMember);
        ownerUserBook.setBook(testBook);
        ownerUserBook.setBookRole(BookRole.OWNER);
        
        editorUserBook = new UserBook();
        editorUserBook.setId(2L);
        editorUserBook.setMember(testMember);
        editorUserBook.setBook(testBook);
        editorUserBook.setBookRole(BookRole.EDITOR);
        
        viewerUserBook = new UserBook();
        viewerUserBook.setId(3L);
        viewerUserBook.setMember(testMember);
        viewerUserBook.setBook(testBook);
        viewerUserBook.setBookRole(BookRole.VIEWER);
    }
    
    @Test
    @DisplayName("가계부 접근 권한 검증 - 정상 케이스 (OWNER)")
    void validateBookAccess_Success_Owner() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        
        // When & Then
        assertDoesNotThrow(() -> categoryService.validateBookAccess(testBook));
    }
    
    @Test
    @DisplayName("가계부 접근 권한 검증 - 정상 케이스 (EDITOR)")
    void validateBookAccess_Success_Editor() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.of(editorUserBook));
        
        // When & Then
        assertDoesNotThrow(() -> categoryService.validateBookAccess(testBook));
    }
    
    @Test
    @DisplayName("가계부 접근 권한 검증 - 정상 케이스 (VIEWER)")
    void validateBookAccess_Success_Viewer() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.of(viewerUserBook));
        
        // When & Then
        assertDoesNotThrow(() -> categoryService.validateBookAccess(testBook));
    }
    
    @Test
    @DisplayName("가계부 접근 권한 검증 - 실패 케이스 (권한 없음)")
    void validateBookAccess_Fail_NoPermission() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.empty());
        
        // When & Then
        BookAccessException exception = assertThrows(BookAccessException.class, 
                () -> categoryService.validateBookAccess(testBook));
        
        assertTrue(exception.getMessage().contains("해당 가계부에 대한 접근 권한이 없습니다"));
    }
    
    @Test
    @DisplayName("가계부 편집 권한 검증 - 정상 케이스 (OWNER)")
    void validateBookEditAccess_Success_Owner() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        
        // When & Then
        assertDoesNotThrow(() -> categoryService.validateBookEditAccess(testBook));
    }
    
    @Test
    @DisplayName("가계부 편집 권한 검증 - 정상 케이스 (EDITOR)")
    void validateBookEditAccess_Success_Editor() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.of(editorUserBook));
        
        // When & Then
        assertDoesNotThrow(() -> categoryService.validateBookEditAccess(testBook));
    }
    
    @Test
    @DisplayName("가계부 편집 권한 검증 - 실패 케이스 (VIEWER)")
    void validateBookEditAccess_Fail_Viewer() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.of(viewerUserBook));
        
        // When & Then
        InsufficientPermissionException exception = assertThrows(InsufficientPermissionException.class, 
                () -> categoryService.validateBookEditAccess(testBook));
        
        assertTrue(exception.getMessage().contains("카테고리를 추가하려면 편집 권한이 필요합니다"));
        assertTrue(exception.getMessage().contains("현재 권한: VIEWER"));
    }
    
    @Test
    @DisplayName("가계부 편집 권한 검증 - 실패 케이스 (권한 없음)")
    void validateBookEditAccess_Fail_NoPermission() {
        // Given
        when(memberService.getCurrentMember()).thenReturn(testMember);
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.empty());
        
        // When & Then
        BookAccessException exception = assertThrows(BookAccessException.class, 
                () -> categoryService.validateBookEditAccess(testBook));
        
        assertTrue(exception.getMessage().contains("해당 가계부에 대한 접근 권한이 없습니다"));
    }
    
    @Test
    @DisplayName("사용자 권한 조회 테스트")
    void getUserBookRole_Test() {
        // Given
        when(userBookJpaRepository.findByBookIdAndMemberId(testBook.getId(), testMember.getId()))
                .thenReturn(Optional.of(ownerUserBook));
        
        // When
        BookRole role = categoryService.getUserBookRole(testBook, testMember);
        
        // Then
        assertEquals(BookRole.OWNER, role);
    }
    
    @Test
    @DisplayName("권한 확인 테스트 - OWNER는 모든 권한 보유")
    void hasPermission_Owner_AllPermissions() {
        // Given
        when(userBookJpaRepository.findByBookIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(ownerUserBook));
        
        // When & Then
        assertTrue(categoryService.hasPermission(testBook, testMember, BookRole.VIEWER));
        assertTrue(categoryService.hasPermission(testBook, testMember, BookRole.EDITOR));
        assertTrue(categoryService.hasPermission(testBook, testMember, BookRole.OWNER));
    }
    
    @Test
    @DisplayName("권한 확인 테스트 - EDITOR는 VIEWER, EDITOR 권한만 보유")
    void hasPermission_Editor_LimitedPermissions() {
        // Given
        when(userBookJpaRepository.findByBookIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(editorUserBook));
        
        // When & Then
        assertTrue(categoryService.hasPermission(testBook, testMember, BookRole.VIEWER));
        assertTrue(categoryService.hasPermission(testBook, testMember, BookRole.EDITOR));
        assertFalse(categoryService.hasPermission(testBook, testMember, BookRole.OWNER));
    }
    
    @Test
    @DisplayName("권한 확인 테스트 - VIEWER는 VIEWER 권한만 보유")
    void hasPermission_Viewer_ViewOnlyPermission() {
        // Given
        when(userBookJpaRepository.findByBookIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(viewerUserBook));
        
        // When & Then
        assertTrue(categoryService.hasPermission(testBook, testMember, BookRole.VIEWER));
        assertFalse(categoryService.hasPermission(testBook, testMember, BookRole.EDITOR));
        assertFalse(categoryService.hasPermission(testBook, testMember, BookRole.OWNER));
    }
}