package garabu.garabuServer.api;


import garabu.garabuServer.domain.Book;
import garabu.garabuServer.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 가계부 관리 API 컨트롤러
 * 
 * 가계부의 생성, 조회 등의 기능을 제공합니다.
 * 사용자는 여러 개의 가계부를 생성하고 관리할 수 있습니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Book", description = "가계부 관리 API")
public class BookApiController {
    
    private final BookService bookService;

    /**
     * 새로운 가계부를 생성합니다.
     * 
     * @param request 가계부 생성 요청 정보
     * @return 생성된 가계부의 ID
     */
    @PostMapping("/api/v2/book")
    @Operation(
        summary = "가계부 생성",
        description = "새로운 가계부를 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "가계부 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateBookResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public CreateBookResponse saveBookV2(
        @Parameter(description = "가계부 생성 정보", required = true)
        @RequestBody @Valid CreateBookRequest request) {
        // BookService 내에서 현재 로그인한 사용자를 기반으로 새로운 Book을 생성
        Book book = bookService.createBook(request.getTitle());
        return new CreateBookResponse(book.getId());
    }

    /**
     * 현재 로그인한 사용자의 가계부 목록을 조회합니다.
     * 
     * @return 사용자의 가계부 목록
     */
    @GetMapping("/api/v2/book/mybooks")
    @Operation(
        summary = "내 가계부 목록 조회",
        description = "현재 로그인한 사용자가 소유한 가계부 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "가계부 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = Book.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<List<Book>> getMyBooks() {
        List<Book> books = bookService.findLoggedInUserBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * 가계부 생성 요청 DTO
     */
    @Data
    static class CreateBookRequest {
        @Parameter(description = "가계부 이름", example = "내 가계부", required = true)
        private String title; // 가계부 이름
    }

    /**
     * 가계부 생성 응답 DTO
     */
    @Data
    static class CreateBookResponse {
        @Parameter(description = "생성된 가계부의 ID")
        private Long id; // 생성된 가계부의 ID

        public CreateBookResponse(Long id) {
            this.id = id;
        }
    }
}
