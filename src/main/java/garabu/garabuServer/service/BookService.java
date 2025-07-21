package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.domain.AssetType;
import garabu.garabuServer.dto.BookDTO;
import garabu.garabuServer.dto.request.CreateAssetRequest;
import garabu.garabuServer.exception.BookNotFoundException;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import garabu.garabuServer.repository.LedgerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 가계부 관리 서비스 클래스
 * 
 * 가계부의 생성, 조회 등의 비즈니스 로직을 처리합니다.
 * 현재 로그인한 사용자와 연관된 가계부를 관리합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final MemberJPARepository memberRepository;
    private final UserBookJpaRepository userBookJpaRepository;
    private final AssetService assetService;
    private final LedgerJpaRepository ledgerJpaRepository;

    /**
     * 새로운 가계부를 생성합니다.
     * 가계부 생성 시 생성자에게 자동으로 OWNER 권한을 부여합니다.
     * 
     * @param title 가계부 제목
     * @return 생성된 가계부 정보
     */
    @Transactional
    @CacheEvict(value = "userBooks", key = "#root.methodName.replace('createBook', 'findLoggedInUserBooks') + '_' + @bookService.getCurrentUserCacheKey()")
    public Book createBook(String title) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("=== 가계부 생성 시작 ===");
        System.out.println("가계부 제목: " + title);
        System.out.println("Username: " + username);
        
        // username으로 사용자 조회
        Member owner = memberRepository.findByUsername(username);
        
        if (owner == null) {
            System.out.println("사용자를 찾을 수 없습니다!");
            throw new RuntimeException("사용자를 찾을 수 없습니다: " + username);
        }
        
        System.out.println("사용자 ID: " + owner.getId());
        System.out.println("사용자 이름: " + owner.getUsername());
        System.out.println("사용자 이메일: " + owner.getEmail());

        // 1. 가계부 생성
        Book book = new Book();
        book.setOwner(owner);
        book.setTitle(title);
        book.setDefaultCurrency("KRW"); // 기본 통화를 원화로 설정
        book.setUseMultiCurrency(false); // 기본적으로 단일 통화 사용
        bookRepository.save(book);
        System.out.println("가계부 생성 완료 - ID: " + book.getId());
        
        // 2. UserBook 생성 - 생성자에게 OWNER 권한 부여
        UserBook userBook = new UserBook();
        userBook.setMember(owner);
        userBook.setBook(book);
        userBook.setBookRole(BookRole.OWNER);
        userBookJpaRepository.save(userBook);
        System.out.println("UserBook 생성 완료 - ID: " + userBook.getId());
        
        // 3. 기본 자산 생성
        try {
            createDefaultAssets(book);
            System.out.println("기본 자산 생성 완료");
        } catch (Exception e) {
            System.err.println("기본 자산 생성 실패: " + e.getMessage());
            // 기본 자산 생성 실패해도 가계부 생성은 계속 진행
        }
        
        System.out.println("=== 가계부 생성 완료 ===");
        return book;
    }

    /**
     * 현재 로그인한 사용자의 이메일을 반환합니다.
     * 
     * @return 로그인한 사용자의 이메일
     */
    private String getEmail(){
        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        return loggedInUser.getName();
    }

    /**
     * 현재 로그인한 사용자가 소유하거나 공유받은 가계부 목록을 조회합니다.
     * Redis 캐싱 적용으로 성능 최적화
     * 
     * @return 사용자의 가계부 목록
     */
    @CacheEvict(value = "userBooks", allEntries = true)
    public void clearUserBooksCache() {
        // 캐시 초기화용 메서드
    }

    @Cacheable(value = "userBooks", key = "#root.methodName + '_' + @bookService.getCurrentUserCacheKey()", unless = "#result == null or #result.isEmpty()")
    @Transactional(readOnly = true)
    public List<BookDTO> findLoggedInUserBooks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("=== 가계부 목록 조회 시작 ===");
        System.out.println("Username: " + username);
        
        // username으로 사용자 조회
        Member currentMember = memberRepository.findByUsername(username);
        
        if (currentMember == null) {
            System.out.println("사용자를 찾을 수 없습니다!");
            return List.of();
        }
        
        System.out.println("사용자 ID: " + currentMember.getId());
        System.out.println("사용자 이름: " + currentMember.getUsername());
        System.out.println("사용자 이메일: " + currentMember.getEmail());
        
        // 사용자가 소유한 가계부와 공유받은 가계부 모두 조회
        List<UserBook> userBooks = userBookJpaRepository.findAllByMember(currentMember);
        System.out.println("참여한 가계부 수: " + userBooks.size());
        
        List<BookDTO> bookDTOs = userBooks.stream()
                .map(userBook -> {
                    Book book = userBook.getBook();
                    BookRole role = userBook.getBookRole();
                    return BookDTO.from(book, role);
                })
                .distinct()
                .toList();
        
        System.out.println("반환할 가계부 수: " + bookDTOs.size());
        System.out.println("=== 가계부 목록 조회 완료 ===");
        
        return bookDTOs;
    }
    
    /**
     * 현재 사용자의 캐시 키를 생성합니다.
     * 
     * @return 캐시 키 문자열
     */
    public String getCurrentUserCacheKey() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return username;
    }

    /**
     * ID로 가계부를 조회합니다.
     * 
     * @param id 조회할 가계부의 ID
     * @return 조회된 가계부 정보
     * @throws BookNotFoundException 가계부를 찾을 수 없는 경우
     */
    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    /**
     * 제목으로 가계부를 조회합니다.
     * 
     * @param title 조회할 가계부의 제목
     * @return 조회된 가계부 정보
     */
    public Book findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }
    
    /**
     * 가계부를 삭제합니다.
     * 소유자만 가계부를 삭제할 수 있으며, 모든 관련 데이터가 함께 삭제됩니다.
     * 
     * @param bookId 삭제할 가계부의 ID
     * @param currentUser 현재 로그인한 사용자
     * @throws IllegalArgumentException 소유자가 아닌 경우
     * @throws BookNotFoundException 가계부를 찾을 수 없는 경우
     */
    @Transactional
    @CacheEvict(value = "userBooks", allEntries = true)
    public void deleteBook(Long bookId, Member currentUser) {
        // 가계부 조회
        Book book = findById(bookId);
        
        // 소유자 권한 확인
        UserBook userBook = userBookJpaRepository.findByBookIdAndMemberId(bookId, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 가계부에 접근할 수 없습니다."));
        
        if (userBook.getBookRole() != BookRole.OWNER) {
            throw new IllegalArgumentException("소유자만 가계부를 삭제할 수 있습니다.");
        }
        
        System.out.println("=== 가계부 삭제 시작 ===");
        System.out.println("삭제할 가계부 ID: " + bookId);
        System.out.println("삭제한 사용자: " + currentUser.getUsername());
        
        // 가계부와 관련된 모든 데이터 삭제 (순서 중요)
        
        // 1. 가계부 내역 삭제 (이미 구현된 메서드 사용)
        ledgerJpaRepository.deleteAllByBook(book);
        System.out.println("가계부 내역 삭제 완료");
        
        // 2. UserBook 관계 삭제 (가계부에 참여한 모든 멤버)
        List<UserBook> userBooks = userBookJpaRepository.findByBookId(bookId);
        userBookJpaRepository.deleteAll(userBooks);
        System.out.println("UserBook 관계 삭제 완료");
        
        // 3. 가계부 삭제 (나머지 관련 데이터는 DB 제약조건으로 처리)
        bookRepository.delete(book);
        System.out.println("가계부 삭제 완료");
        
        System.out.println("=== 가계부 삭제 완료 ===");
    }
    
    /**
     * 가계부 생성 시 기본 자산을 자동으로 생성합니다.
     * 모든 기본 자산은 0원으로 시작합니다.
     * 
     * @param book 자산을 생성할 가계부
     */
    private void createDefaultAssets(Book book) {
        // 현금 자산 생성
        CreateAssetRequest cashRequest = new CreateAssetRequest();
        cashRequest.setName("현금");
        cashRequest.setAssetType(AssetType.CASH);
        cashRequest.setBalance(0L);
        cashRequest.setDescription("현금 자산");
        assetService.createAsset(book.getId(), cashRequest);
        
        // 체크카드 자산 생성
        CreateAssetRequest debitCardRequest = new CreateAssetRequest();
        debitCardRequest.setName("체크카드");
        debitCardRequest.setAssetType(AssetType.DEBIT_CARD);
        debitCardRequest.setBalance(0L);
        debitCardRequest.setDescription("체크카드 자산");
        assetService.createAsset(book.getId(), debitCardRequest);
        
        // 신용카드 자산 생성
        CreateAssetRequest creditCardRequest = new CreateAssetRequest();
        creditCardRequest.setName("신용카드");
        creditCardRequest.setAssetType(AssetType.CREDIT_CARD);
        creditCardRequest.setBalance(0L);
        creditCardRequest.setDescription("신용카드 자산");
        assetService.createAsset(book.getId(), creditCardRequest);
        
        // 저축예금 자산 생성
        CreateAssetRequest savingsRequest = new CreateAssetRequest();
        savingsRequest.setName("저축예금");
        savingsRequest.setAssetType(AssetType.SAVINGS_ACCOUNT);
        savingsRequest.setBalance(0L);
        savingsRequest.setDescription("저축예금 계좌");
        assetService.createAsset(book.getId(), savingsRequest);
    }
    
    //book. 가계부를 커플, 개인, 모임용으로 나누기 위해.
    //사용자 입력
    //가계부 이름 등록
    //
}
