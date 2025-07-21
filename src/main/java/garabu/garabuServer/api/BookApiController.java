package garabu.garabuServer.api;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.BookDTO;
import garabu.garabuServer.event.BookEvent;
import garabu.garabuServer.event.BookEventPublisher;
import garabu.garabuServer.service.BookService;
import garabu.garabuServer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 가계부(Book) 관리 REST 컨트롤러
 *
 * <p>JWT Bearer 토큰 인증을 사용하며,
 * 엔드포인트 버전은 /api/v2 로 통일합니다.</p>
 *
 * @author Garabu Team
 * @version 2.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book")                            // 공통 prefix
@Tag(name = "Book", description = "가계부 관리 API")
@SecurityRequirement(name = "bearerAuth")                  // Swagger UI의 Authorize 버튼과 매핑
public class BookApiController {

    private final BookService bookService;
    private final BookEventPublisher bookEventPublisher;
    private final MemberService memberService;

    // ───────────────────────── 가계부 생성 ─────────────────────────
    /**
     * 새로운 가계부를 생성합니다.
     *
     * @param request 가계부 생성 요청 DTO
     * @return 생성된 가계부의 ID
     */
    @PostMapping
    @Operation(
            summary     = "가계부 생성",
            description = "새로운 가계부(Book)를 생성하고 고유 ID를 반환합니다."
    )
    @RequestBody(
            required = true,
            description = "가계부 생성 요청 본문",
            content = @Content(
                    schema = @Schema(implementation = CreateBookRequest.class),
                    examples = @ExampleObject(
                            name  = "예시",
                            value = "{ \"title\": \"내 첫 번째 가계부\" }"
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description  = "가계부 생성 성공",
                    content      = @Content(schema = @Schema(implementation = CreateBookResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CreateBookResponse> saveBookV2(
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateBookRequest request) {

        // 현재 사용자 정보 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Book book = bookService.createBook(request.getTitle()); // 로그인 사용자 기반 생성
        
        // Redis 이벤트 발행 - 가계부 생성 이벤트
        BookEvent event = BookEvent.builder()
                .bookId(book.getId())
                .eventType("BOOK_CREATED")
                .data(new CreateBookResponse(book.getId(), book.getTitle()))
                .userId(currentUser.getId())
                .timestamp(System.currentTimeMillis())
                .build();
        bookEventPublisher.publishBookEvent(event);
        
        return ResponseEntity
                .status(201)
                .body(new CreateBookResponse(book.getId(), book.getTitle()));
    }

    // ───────────────────────── 내 가계부 목록 조회 ─────────────────────────
    /**
     * 현재 로그인 사용자의 가계부 목록을 조회합니다.
     *
     * @return 가계부 리스트
     */
    @GetMapping("/mybooks")
    @Operation(
            summary     = "내 가계부 목록 조회",
            description = "현재 로그인 사용자가 소유한 모든 가계부(Book) 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "가계부 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = BookDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<BookDTO>> getMyBooks() {
        List<BookDTO> books = bookService.findLoggedInUserBooks();
        return ResponseEntity.ok(books);
    }

    // ───────────────────────── 가계부 삭제 ─────────────────────────
    /**
     * 가계부를 삭제합니다.
     * 소유자만 가계부를 삭제할 수 있습니다.
     *
     * @param bookId 삭제할 가계부의 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/{bookId}")
    @Operation(
            summary     = "가계부 삭제",
            description = "가계부를 삭제합니다. 소유자만 삭제할 수 있으며, 모든 관련 데이터가 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "가계부 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자만 삭제 가능)"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) {
        // 현재 사용자 정보 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        bookService.deleteBook(bookId, currentUser);
        
        // Redis 이벤트 발행 - 가계부 삭제 이벤트
        BookEvent event = BookEvent.builder()
                .bookId(bookId)
                .eventType("BOOK_DELETED")
                .userId(currentUser.getId())
                .timestamp(System.currentTimeMillis())
                .build();
        bookEventPublisher.publishBookEvent(event);
        
        return ResponseEntity.noContent().build();
    }

    // ───────────────────────── DTO 정의 ─────────────────────────
    /**
     * 가계부 생성 요청 DTO
     */
    @Data
    @Schema(description = "가계부 생성 요청 DTO")
    static class CreateBookRequest {
        @Schema(description = "가계부 이름", example = "우리 가족 가계부", requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;
    }

    /**
     * 가계부 생성 응답 DTO
     */
    @Data
    @Schema(description = "가계부 생성 응답 DTO")
    static class CreateBookResponse {
        @Schema(description = "생성된 가계부 ID", example = "42")
        private Long id;
        
        @Schema(description = "가계부 제목", example = "우리 가족 가계부")
        private String title;

        public CreateBookResponse(Long id, String title) {
            this.id = id;
            this.title = title;
        }
    }
}
