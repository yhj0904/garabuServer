package garabu.garabuServer.api;

import garabu.garabuServer.domain.Memo;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.MemoRepository;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v2/memos")
@RequiredArgsConstructor
@Tag(name = "Memo", description = "가계부 메모 API")
@SecurityRequirement(name = "bearerAuth")
public class MemoApiController {
    
    private final MemoRepository memoRepository;
    private final BookRepository bookRepository;
    private final MemberService memberService;
    
    @GetMapping("/book/{bookId}")
    @Operation(summary = "가계부 메모 조회", description = "가계부의 메모를 조회합니다. 메모가 없는 경우 null을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메모 조회 성공",
            content = @Content(schema = @Schema(implementation = MemoResponse.class))),
        @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<MemoResponse> getBookMemo(
            @Parameter(description = "가계부 ID", required = true) @PathVariable Long bookId) {
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        return memoRepository.findByBook(book)
            .map(memo -> ResponseEntity.ok(convertToMemoResponse(memo)))
            .orElse(ResponseEntity.ok(null));
    }
    
    @PostMapping("/book/{bookId}")
    @Operation(summary = "가계부 메모 생성", description = "가계부에 메모를 작성합니다. 이미 메모가 있는 경우 업데이트됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메모 작성/수정 성공",
            content = @Content(schema = @Schema(implementation = MemoResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    @Transactional
    public ResponseEntity<MemoResponse> createOrUpdateBookMemo(
            @Parameter(description = "가계부 ID", required = true) @PathVariable Long bookId,
            @Parameter(description = "메모 생성/수정 요청", required = true) @Valid @RequestBody CreateMemoRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        Memo memo = memoRepository.findByBook(book).orElse(null);
        
        if (memo == null) {
            // 새 메모 생성
            memo = new Memo();
            memo.setBook(book);
            memo.setAuthor(currentUser);
            memo.setTitle(request.getTitle());
            memo.setContent(request.getContent());
            memo.setImportant(request.isImportant());
            memo.setColor(request.getColor());
        } else {
            // 기존 메모 업데이트
            memo.update(request.getTitle(), request.getContent(), currentUser);
            memo.setImportant(request.isImportant());
            memo.setColor(request.getColor());
        }
        
        Memo savedMemo = memoRepository.save(memo);
        
        return ResponseEntity.ok(convertToMemoResponse(savedMemo));
    }
    
    @DeleteMapping("/book/{bookId}")
    @Operation(summary = "가계부 메모 삭제", description = "가계부의 메모를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메모 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "메모 또는 가계부를 찾을 수 없음")
    })
    @Transactional
    public ResponseEntity<ApiResponseMessage> deleteBookMemo(
            @Parameter(description = "가계부 ID", required = true) @PathVariable Long bookId) {
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
        
        Memo memo = memoRepository.findByBook(book)
            .orElseThrow(() -> new IllegalArgumentException("메모가 없습니다."));
        
        memoRepository.delete(memo);
        
        return ResponseEntity.ok(new ApiResponseMessage("메모가 삭제되었습니다.", LocalDateTime.now()));
    }
    
    private MemoResponse convertToMemoResponse(Memo memo) {
        return new MemoResponse(
            memo.getId(),
            memo.getBook().getId(),
            memo.getTitle(),
            memo.getContent(),
            memo.isImportant(),
            memo.getColor(),
            memo.getAuthor().getId(),
            memo.getAuthor().getName(),
            memo.getLastEditor() != null ? memo.getLastEditor().getId() : null,
            memo.getLastEditor() != null ? memo.getLastEditor().getName() : null,
            memo.getCreatedAt(),
            memo.getUpdatedAt()
        );
    }
    
    @Data
    @Schema(description = "메모 생성/수정 요청")
    public static class CreateMemoRequest {
        @Schema(description = "메모 제목 (선택사항)", example = "2025년 1월 가계부 목표", maxLength = 100)
        private String title;
        
        @Schema(description = "메모 내용", example = "이번 달 목표: 외식비 20만원 이하로 줄이기", required = true, minLength = 1, maxLength = 5000)
        private String content;
        
        @Schema(description = "중요 표시 여부", example = "false")
        private boolean important = false;
        
        @Schema(description = "메모 색상 (선택사항)", example = "#FFE4B5")
        private String color;
    }
    
    @Data
    @Schema(description = "메모 응답")
    public static class MemoResponse {
        @Schema(description = "메모 ID", example = "1")
        private Long id;
        
        @Schema(description = "가계부 ID", example = "123")
        private Long bookId;
        
        @Schema(description = "메모 제목", example = "2025년 1월 가계부 목표")
        private String title;
        
        @Schema(description = "메모 내용", example = "이번 달 목표: 외식비 20만원 이하로 줄이기")
        private String content;
        
        @Schema(description = "중요 표시 여부", example = "false")
        private boolean important;
        
        @Schema(description = "메모 색상", example = "#FFE4B5")
        private String color;
        
        @Schema(description = "작성자 ID", example = "456")
        private Long authorId;
        
        @Schema(description = "작성자 이름", example = "홍길동")
        private String authorName;
        
        @Schema(description = "마지막 수정자 ID", example = "789")
        private Long lastEditorId;
        
        @Schema(description = "마지막 수정자 이름", example = "김철수")
        private String lastEditorName;
        
        @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;
        
        @Schema(description = "수정일시", example = "2024-01-16T14:20:00")
        private LocalDateTime updatedAt;
        
        public MemoResponse(Long id, Long bookId, String title, String content, boolean important, 
                          String color, Long authorId, String authorName, Long lastEditorId, 
                          String lastEditorName, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.bookId = bookId;
            this.title = title;
            this.content = content;
            this.important = important;
            this.color = color;
            this.authorId = authorId;
            this.authorName = authorName;
            this.lastEditorId = lastEditorId;
            this.lastEditorName = lastEditorName;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
    
    @Data
    @Schema(description = "API 응답 메시지")
    public static class ApiResponseMessage {
        @Schema(description = "응답 메시지", example = "메모가 삭제되었습니다.")
        private String message;
        
        @Schema(description = "작업 시간", example = "2024-01-15T10:30:00")
        private LocalDateTime timestamp;
        
        public ApiResponseMessage(String message, LocalDateTime timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
    }
}