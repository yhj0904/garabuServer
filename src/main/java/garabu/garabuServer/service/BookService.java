package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.UserBookJpaRepository;
import lombok.RequiredArgsConstructor;
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

    /**
     * 새로운 가계부를 생성합니다.
     * 가계부 생성 시 생성자에게 자동으로 OWNER 권한을 부여합니다.
     * 
     * @param title 가계부 제목
     * @return 생성된 가계부 정보
     */
    @Transactional
    public Book createBook(String title) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Member owner = memberRepository.findByUsername(currentUserName);

        // 1. 가계부 생성
        Book book = new Book();
        book.setOwner(owner);
        book.setTitle(title);
        bookRepository.save(book);
        
        // 2. UserBook 생성 - 생성자에게 OWNER 권한 부여
        UserBook userBook = new UserBook();
        userBook.setMember(owner);
        userBook.setBook(book);
        userBook.setBookRole(BookRole.OWNER);
        userBookJpaRepository.save(userBook);
        
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
     * 현재 로그인한 사용자가 소유한 가계부 목록을 조회합니다.
     * 
     * @return 사용자의 가계부 목록
     */
    public List<Book> findLoggedInUserBooks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getEmail();
        String providerId = userDetails.getProviderId();
        
        Member owner;
        if (providerId != null) {
            // 소셜로그인 사용자
            owner = memberRepository.findByEmailAndProviderId(email, providerId);
        } else {
            // 일반로그인 사용자 (providerId가 null)
            owner = memberRepository.findByEmailAndProviderIdIsNull(email);
        }
        
        return bookRepository.findByOwner(owner);
    }

    /**
     * ID로 가계부를 조회합니다.
     * 
     * @param id 조회할 가계부의 ID
     * @return 조회된 가계부 정보
     * @throws RuntimeException 가계부를 찾을 수 없는 경우
     */
    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
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
    
    //book. 가계부를 커플, 개인, 모임용으로 나누기 위해.
    //사용자 입력
    //가계부 이름 등록
    //
}
