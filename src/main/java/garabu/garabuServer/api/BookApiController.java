package garabu.garabuServer.api;


import garabu.garabuServer.domain.Book;
import garabu.garabuServer.service.BookService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookApiController {
    private final BookService bookService;

    @PostMapping("/api/v2/book")
    public CreateBookResponse saveBookV2(@RequestBody @Valid
                                         CreateBookRequest request) {

        Book book = bookService.createBook(request.getMemberId(), request.getBookName());
        return new CreateBookResponse(book.getId());
    }

    @Data
    static class CreateBookRequest {
        private Long memberId; // 가계부를 생성하는 사용자의 ID
        private String bookName; // 가계부 이름
    }

    @Data
    static class CreateBookResponse {
        private Long id; // 생성된 가계부의 ID

        public CreateBookResponse(Long id) {
            this.id = id;
        }
    }
}
