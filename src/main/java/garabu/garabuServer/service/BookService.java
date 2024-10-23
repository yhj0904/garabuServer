package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final MemberJPARepository memberRepository;

    public Book createBook(String title) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Member owner = memberRepository.findByUsername(currentUserName);

        Book book = new Book();
        book.setOwner(owner);
        book.setTitle(title);

        bookRepository.save(book);
        return book;
    }

    private String getEmail(){
        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        return loggedInUser.getName();
    }

    public List<Book> findLoggedInUserBooks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getEmail();
        String username = userDetails.getUsername();
        String name = userDetails.getName();
        Member ownerName = memberRepository.findByName(name);
        List<Member> owner = memberRepository.findByEmail(email);
        return bookRepository.findByOwner((Member) owner);
    }


    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }
    //book. 가계부를 커플, 개인, 모임용으로 나누기 위해.
    //사용자 입력
    //가계부 이름 등록
    //

}
