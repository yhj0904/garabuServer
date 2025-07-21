package garabu.garabuServer.api;

import garabu.garabuServer.dto.tag.*;
import garabu.garabuServer.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/tags")
@RequiredArgsConstructor
@Tag(name = "Tag", description = "태그 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class TagApiController {
    
    private final TagService tagService;
    
    @PostMapping("/books/{bookId}")
    @Operation(summary = "태그 생성", description = "새로운 태그를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "태그 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 태그")
    })
    public ResponseEntity<TagResponse> createTag(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Valid @RequestBody CreateTagRequest request) {
        
        TagResponse response = tagService.createTag(bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/books/{bookId}")
    @Operation(summary = "태그 목록 조회", description = "가계부의 태그 목록을 사용 빈도순으로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<List<TagResponse>> getTags(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId) {
        
        List<TagResponse> tags = tagService.getTags(bookId);
        return ResponseEntity.ok(tags);
    }
    
    @PutMapping("/{tagId}")
    @Operation(summary = "태그 수정", description = "태그 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "중복된 태그 이름")
    })
    public ResponseEntity<TagResponse> updateTag(
            @Parameter(description = "태그 ID") @PathVariable Long tagId,
            @Valid @RequestBody UpdateTagRequest request) {
        
        TagResponse response = tagService.updateTag(tagId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{tagId}")
    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "태그 ID") @PathVariable Long tagId) {
        
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/transactions/{ledgerId}/tags/{tagId}")
    @Operation(summary = "거래 내역에 태그 추가", description = "거래 내역에 태그를 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "추가 성공"),
        @ApiResponse(responseCode = "404", description = "거래 내역 또는 태그를 찾을 수 없음")
    })
    public ResponseEntity<Void> addTagToTransaction(
            @Parameter(description = "거래 내역 ID") @PathVariable Long ledgerId,
            @Parameter(description = "태그 ID") @PathVariable Long tagId) {
        
        tagService.addTagToTransaction(ledgerId, tagId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/transactions/{ledgerId}/tags/{tagId}")
    @Operation(summary = "거래 내역에서 태그 제거", description = "거래 내역에서 태그를 제거합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "제거 성공"),
        @ApiResponse(responseCode = "404", description = "거래 내역 또는 태그를 찾을 수 없음")
    })
    public ResponseEntity<Void> removeTagFromTransaction(
            @Parameter(description = "거래 내역 ID") @PathVariable Long ledgerId,
            @Parameter(description = "태그 ID") @PathVariable Long tagId) {
        
        tagService.removeTagFromTransaction(ledgerId, tagId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/books/{bookId}/search")
    @Operation(summary = "태그로 거래 내역 검색", description = "특정 태그가 지정된 거래 내역을 검색합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음")
    })
    public ResponseEntity<TagSearchResponse> searchTransactionsByTag(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Parameter(description = "태그 이름") @RequestParam String tagName,
            @PageableDefault(size = 20) Pageable pageable) {
        
        TagSearchResponse response = tagService.searchTransactionsByTag(bookId, tagName, pageable);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/merge")
    @Operation(summary = "태그 병합", description = "원본 태그를 대상 태그로 병합합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "병합 성공"),
        @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "같은 가계부의 태그가 아님")
    })
    public ResponseEntity<Void> mergeTags(
            @Parameter(description = "원본 태그 ID") @RequestParam Long sourceTagId,
            @Parameter(description = "대상 태그 ID") @RequestParam Long targetTagId) {
        
        tagService.mergeTags(sourceTagId, targetTagId);
        return ResponseEntity.noContent().build();
    }
} 