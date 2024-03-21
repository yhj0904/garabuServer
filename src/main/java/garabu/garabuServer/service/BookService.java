package garabu.garabuServer.service;

import garabu.garabuServer.domain.Book;
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
    private final MemberRepository memberRepository;

    public Long registBook(Book book) {
        bookRepository.save(book);
        return book.getId();
    }

    //book. 가계부를 커플, 개인, 모임용으로 나누기 위해.
    //사용자 입력
    //가계부 이름 등록
    //

}
