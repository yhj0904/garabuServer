package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final MemberJPARepository memberRepository;

    public Book createBook(String bookName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Member owner = memberRepository.findByUsername(currentUserName);

        Book book = new Book();
        book.setOwner(owner);
        book.setBookName(bookName);

        bookRepository.save(book);
        return book;
    }

    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book findByName(String name) {
        return bookRepository.findByName(name);
    }
    //book. 가계부를 커플, 개인, 모임용으로 나누기 위해.
    //사용자 입력
    //가계부 이름 등록
    //

}
