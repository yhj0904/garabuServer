package garabu.garabuServer.api;


import garabu.garabuServer.domain.Book;
import garabu.garabuServer.service.BookService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookApiController {
    private final BookService bookService;

    @PostMapping("/api/v2/book")
    public CreateBookResponse saveBookV2(@RequestBody @Valid CreateBookRequest request) {
        // BookService 내에서 현재 로그인한 사용자를 기반으로 새로운 Book을 생성
        Book book = bookService.createBook(request.getTitle());
        return new CreateBookResponse(book.getId());
    }

    @GetMapping("/api/v2/book/mybooks")
    public ResponseEntity<List<Book>> getMyBooks() {
        List<Book> books = bookService.findLoggedInUserBooks();
        return ResponseEntity.ok(books);
    }

    @Data
    static class CreateBookRequest {
        private String title; // 가계부 이름
    }

    @Data
    static class CreateBookResponse {
        private Long id; // 생성된 가계부의 ID

        public CreateBookResponse(Long id) {
            this.id = id;
        }
    }
}
