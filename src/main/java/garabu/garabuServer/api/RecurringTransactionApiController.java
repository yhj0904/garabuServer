package garabu.garabuServer.api;

import garabu.garabuServer.dto.recurring.*;
import garabu.garabuServer.service.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v2/recurring-transactions")
@RequiredArgsConstructor
@Tag(name = "RecurringTransaction", description = "반복 거래 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class RecurringTransactionApiController {
    
    private final RecurringTransactionService recurringTransactionService;
    
    @PostMapping("/books/{bookId}")
    @Operation(summary = "반복 거래 생성", description = "새로운 반복 거래를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "반복 거래 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<RecurringTransactionResponse> createRecurringTransaction(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Valid @RequestBody CreateRecurringTransactionRequest request) {
        
        RecurringTransactionResponse response = recurringTransactionService.create(bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/books/{bookId}")
    @Operation(summary = "반복 거래 목록 조회", description = "가계부의 반복 거래 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<List<RecurringTransactionResponse>> getRecurringTransactions(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Parameter(description = "활성 상태만 조회") @RequestParam(required = false) Boolean active) {
        
        List<RecurringTransactionResponse> transactions = recurringTransactionService.getByBookId(bookId, active);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{recurringId}")
    @Operation(summary = "반복 거래 상세 조회", description = "특정 반복 거래의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "반복 거래를 찾을 수 없음")
    })
    public ResponseEntity<RecurringTransactionDetailResponse> getRecurringTransactionDetail(
            @Parameter(description = "반복 거래 ID") @PathVariable Long recurringId) {
        
        RecurringTransactionDetailResponse response = recurringTransactionService.getDetail(recurringId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{recurringId}")
    @Operation(summary = "반복 거래 수정", description = "반복 거래 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "반복 거래를 찾을 수 없음")
    })
    public ResponseEntity<RecurringTransactionResponse> updateRecurringTransaction(
            @Parameter(description = "반복 거래 ID") @PathVariable Long recurringId,
            @Valid @RequestBody UpdateRecurringTransactionRequest request) {
        
        RecurringTransactionResponse response = recurringTransactionService.update(recurringId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{recurringId}/pause")
    @Operation(summary = "반복 거래 일시 중지", description = "반복 거래를 일시 중지합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "일시 중지 성공"),
        @ApiResponse(responseCode = "404", description = "반복 거래를 찾을 수 없음")
    })
    public ResponseEntity<RecurringTransactionResponse> pauseRecurringTransaction(
            @Parameter(description = "반복 거래 ID") @PathVariable Long recurringId) {
        
        RecurringTransactionResponse response = recurringTransactionService.pause(recurringId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{recurringId}/resume")
    @Operation(summary = "반복 거래 재개", description = "일시 중지된 반복 거래를 재개합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재개 성공"),
        @ApiResponse(responseCode = "404", description = "반복 거래를 찾을 수 없음")
    })
    public ResponseEntity<RecurringTransactionResponse> resumeRecurringTransaction(
            @Parameter(description = "반복 거래 ID") @PathVariable Long recurringId) {
        
        RecurringTransactionResponse response = recurringTransactionService.resume(recurringId);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{recurringId}")
    @Operation(summary = "반복 거래 삭제", description = "반복 거래를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "반복 거래를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteRecurringTransaction(
            @Parameter(description = "반복 거래 ID") @PathVariable Long recurringId) {
        
        recurringTransactionService.delete(recurringId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{recurringId}/execute")
    @Operation(summary = "반복 거래 수동 실행", description = "반복 거래를 수동으로 실행합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "실행 성공"),
        @ApiResponse(responseCode = "404", description = "반복 거래를 찾을 수 없음")
    })
    public ResponseEntity<RecurringExecutionResponse> executeRecurringTransaction(
            @Parameter(description = "반복 거래 ID") @PathVariable Long recurringId,
            @Parameter(description = "실행 날짜") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate executionDate) {
        
        if (executionDate == null) {
            executionDate = LocalDate.now();
        }
        
        RecurringExecutionResponse response = recurringTransactionService.executeManually(recurringId, executionDate);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/books/{bookId}/upcoming")
    @Operation(summary = "예정된 반복 거래 조회", description = "향후 실행 예정인 반복 거래를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ResponseEntity<List<UpcomingTransactionResponse>> getUpcomingTransactions(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Parameter(description = "조회할 일 수") @RequestParam(defaultValue = "30") int days) {
        
        List<UpcomingTransactionResponse> upcoming = recurringTransactionService.getUpcoming(bookId, days);
        return ResponseEntity.ok(upcoming);
    }
} 