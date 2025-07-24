package garabu.garabuServer.api;

import garabu.garabuServer.domain.Comment;
import garabu.garabuServer.domain.CommentType;
import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.repository.CommentRepository;
import garabu.garabuServer.repository.BookRepository;
import garabu.garabuServer.repository.LedgerJpaRepository;
import garabu.garabuServer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "거래 내역 댓글 API")
@SecurityRequirement(name = "bearerAuth")
public class CommentApiController {
    
    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final LedgerJpaRepository ledgerRepository;
    private final MemberService memberService;
    
    @PostMapping("/ledger/{ledgerId}")
    @Operation(summary = "가계부 내역 댓글 작성", description = "가계부 거래내역에 댓글을 작성합니다. 인증된 사용자만 댓글을 작성할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 작성 성공",
            content = @Content(schema = @Schema(implementation = CommentResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "가계부 내역을 찾을 수 없음")
    })
    @Transactional
    public ResponseEntity<CommentResponse> createLedgerComment(
            @Parameter(description = "가계부 거래내역 ID", required = true) @PathVariable Long ledgerId,
            @Parameter(description = "댓글 생성 요청", required = true) @Valid @RequestBody CreateCommentRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Ledger ledger = ledgerRepository.findById(ledgerId)
            .orElseThrow(() -> new IllegalArgumentException("가계부 내역을 찾을 수 없습니다."));
        
        Comment comment = new Comment();
        comment.setAuthor(currentUser);
        comment.setBook(ledger.getBook());
        comment.setLedger(ledger);
        comment.setCommentType(CommentType.LEDGER);
        comment.setContent(request.getContent());
        
        Comment savedComment = commentRepository.save(comment);
        
        return ResponseEntity.ok(new CommentResponse(
            savedComment.getId(),
            "댓글이 작성되었습니다.",
            LocalDateTime.now()
        ));
    }
    
    @GetMapping("/ledger/{ledgerId}")
    @Operation(summary = "가계부 내역 댓글 목록 조회", description = "가계부 거래내역의 댓글 목록을 페이지 단위로 조회합니다. 삭제된 댓글도 포함되며, deleted 필드로 확인할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = CommentListResponse.class))),
        @ApiResponse(responseCode = "404", description = "가계부 내역을 찾을 수 없음")
    })
    public ResponseEntity<CommentListResponse> getLedgerComments(
            @Parameter(description = "가계부 거래내역 ID", required = true) @PathVariable Long ledgerId,
            @Parameter(description = "페이지네이션 정보") @PageableDefault(size = 20) Pageable pageable) {
        
        Ledger ledger = ledgerRepository.findById(ledgerId)
            .orElseThrow(() -> new IllegalArgumentException("가계부 내역을 찾을 수 없습니다."));
        
        Page<Comment> comments = commentRepository.findByLedger(ledger, pageable);
        
        List<CommentDto> commentDtos = comments.getContent().stream()
            .map(this::convertToCommentDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new CommentListResponse(
            commentDtos,
            comments.getTotalElements(),
            comments.getTotalPages(),
            comments.getNumber(),
            comments.getSize()
        ));
    }
    
    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 실제로 데이터베이스에서 삭제되지 않고 deleted 플래그가 true로 설정됩니다. 본인이 작성한 댓글만 삭제할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 삭제 성공",
            content = @Content(schema = @Schema(implementation = CommentResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @Transactional
    public ResponseEntity<CommentResponse> deleteComment(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        
        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }
        
        comment.delete();
        commentRepository.save(comment);
        
        return ResponseEntity.ok(new CommentResponse(
            commentId,
            "댓글이 삭제되었습니다.",
            LocalDateTime.now()
        ));
    }
    
    private CommentDto convertToCommentDto(Comment comment) {
        return new CommentDto(
            comment.getId(),
            comment.getAuthor().getId(),
            comment.getAuthor().getName(),
            comment.getContent(),
            comment.getCommentType().name(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            comment.isDeleted()
        );
    }
    
    @Data
    @Schema(description = "댓글 생성 요청")
    public static class CreateCommentRequest {
        @Schema(description = "댓글 내용", example = "좋은 거래였습니다!", required = true, minLength = 1, maxLength = 500)
        private String content;
    }
    
    @Data
    @Schema(description = "댓글 정보")
    public static class CommentDto {
        @Schema(description = "댓글 ID", example = "1")
        private Long id;
        @Schema(description = "작성자 ID", example = "123")
        private Long authorId;
        @Schema(description = "작성자 이름", example = "홍길동")
        private String authorName;
        @Schema(description = "댓글 내용", example = "좋은 거래였습니다!")
        private String content;
        @Schema(description = "댓글 타입", example = "BOOK", allowableValues = {"BOOK", "LEDGER"})
        private String commentType;
        @Schema(description = "작성일시", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;
        @Schema(description = "수정일시", example = "2024-01-15T10:35:00")
        private LocalDateTime updatedAt;
        @Schema(description = "삭제 여부", example = "false")
        private boolean deleted;
        
        public CommentDto(Long id, Long authorId, String authorName, String content, String commentType,
                         LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {
            this.id = id;
            this.authorId = authorId;
            this.authorName = authorName;
            this.content = content;
            this.commentType = commentType;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.deleted = deleted;
        }
    }
    
    @Data
    @Schema(description = "댓글 작업 응답")
    public static class CommentResponse {
        @Schema(description = "댓글 ID", example = "1")
        private Long commentId;
        @Schema(description = "응답 메시지", example = "댓글이 작성되었습니다.")
        private String message;
        @Schema(description = "작업 시간", example = "2024-01-15T10:30:00")
        private LocalDateTime timestamp;
        
        public CommentResponse(Long commentId, String message, LocalDateTime timestamp) {
            this.commentId = commentId;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    @Data
    @Schema(description = "댓글 목록 응답")
    public static class CommentListResponse {
        @Schema(description = "댓글 목록")
        private List<CommentDto> comments;
        @Schema(description = "전체 댓글 수", example = "100")
        private long totalElements;
        @Schema(description = "전체 페이지 수", example = "5")
        private int totalPages;
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int currentPage;
        @Schema(description = "페이지 크기", example = "20")
        private int size;
        
        public CommentListResponse(List<CommentDto> comments, long totalElements, 
                                 int totalPages, int currentPage, int size) {
            this.comments = comments;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }
    }
}