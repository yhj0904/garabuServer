package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.BookType;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.MemberJPARepository;
import garabu.garabuServer.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final MemberJPARepository memberRepository;

    public Book createBook(Long memberId, String bookName, BookType bookType) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        Book book = new Book();
        book.setOwner(member);
        book.setBookName(bookName);
        book.setBookType(bookType);

        bookRepository.save(book);
        return book;
    }
    //book. 가계부를 커플, 개인, 모임용으로 나누기 위해.
    //사용자 입력
    //가계부 이름 등록
    //

}
