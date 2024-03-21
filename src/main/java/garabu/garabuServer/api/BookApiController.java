package garabu.garabuServer.api;


import garabu.garabuServer.domain.Book;
import garabu.garabuServer.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
        Book book = new Book();
        book.setBookName(request.getBookName());
        Long id = bookService.registBook(book);
        return new CreateBookResponse(id);
    }



    @Data
    static class CreateBookRequest {
        private String BookName;

    }

    @Data
    static class CreateBookResponse {
        private Long id;
        public CreateBookResponse(Long id) {
            this.id = id;
        }
    }
}
