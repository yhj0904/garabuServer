package garabu.garabuServer.api;

import garabu.garabuServer.dto.BudgetRequest;
import garabu.garabuServer.dto.BudgetResponse;
import garabu.garabuServer.dto.BudgetSummaryResponse;
import garabu.garabuServer.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/budgets")
@RequiredArgsConstructor
@Tag(name = "예산 관리", description = "가계부별 예산 관리 API")
public class BudgetApiController {

    private final BudgetService budgetService;

    @PostMapping("/books/{bookId}")
    @Operation(
            summary = "예산 생성",
            description = "특정 가계부에 새로운 예산을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "예산 생성 성공",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<BudgetResponse> createBudget(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId,
            @RequestBody BudgetRequest request) {
        
        BudgetResponse response = budgetService.createBudget(bookId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/books/{bookId}/months/{budgetMonth}")
    @Operation(
            summary = "예산 수정",
            description = "특정 가계부의 특정 년월 예산을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "예산 수정 성공",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "예산을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<BudgetResponse> updateBudget(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "예산 년월", example = "2025-01")
            @PathVariable String budgetMonth,
            @RequestBody BudgetRequest request) {
        
        BudgetResponse response = budgetService.updateBudget(bookId, budgetMonth, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/books/{bookId}/months/{budgetMonth}")
    @Operation(
            summary = "예산 삭제",
            description = "특정 가계부의 특정 년월 예산을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예산 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "예산을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Void> deleteBudget(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "예산 년월", example = "2025-01")
            @PathVariable String budgetMonth) {
        
        budgetService.deleteBudget(bookId, budgetMonth);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books/{bookId}")
    @Operation(
            summary = "가계부별 예산 목록 조회",
            description = "특정 가계부의 모든 예산 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "예산 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<BudgetResponse>> getBudgetsByBook(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId) {
        
        List<BudgetResponse> budgets = budgetService.getBudgetsByBook(bookId);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/books/{bookId}/months/{budgetMonth}")
    @Operation(
            summary = "특정 년월 예산 조회",
            description = "특정 가계부의 특정 년월 예산을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "예산 조회 성공",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "예산을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<BudgetResponse> getBudgetByMonth(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "예산 년월", example = "2025-01")
            @PathVariable String budgetMonth) {
        
        BudgetResponse budget = budgetService.getBudgetByMonth(bookId, budgetMonth);
        if (budget == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/books/{bookId}/months/{budgetMonth}/summary")
    @Operation(
            summary = "예산 요약 정보 조회",
            description = "특정 가계부의 특정 년월 예산과 실제 수입/지출을 비교한 요약 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "예산 요약 조회 성공",
                    content = @Content(schema = @Schema(implementation = BudgetSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "예산을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<BudgetSummaryResponse> getBudgetSummary(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "예산 년월", example = "2025-01")
            @PathVariable String budgetMonth) {
        
        BudgetSummaryResponse summary = budgetService.getBudgetSummary(bookId, budgetMonth);
        if (summary == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/books/{bookId}/years/{year}")
    @Operation(
            summary = "연도별 예산 목록 조회",
            description = "특정 가계부의 특정 연도 예산 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "연도별 예산 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<BudgetResponse>> getBudgetsByYear(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "연도", example = "2025")
            @PathVariable String year) {
        
        List<BudgetResponse> budgets = budgetService.getBudgetsByYear(bookId, year);
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/books/{bookId}/recent")
    @Operation(
            summary = "최근 예산 목록 조회",
            description = "특정 가계부의 최근 N개월 예산 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "최근 예산 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BudgetResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<BudgetResponse>> getRecentBudgets(
            @Parameter(description = "가계부 ID", example = "1")
            @PathVariable Long bookId,
            @Parameter(description = "조회할 개월 수", example = "6")
            @RequestParam(defaultValue = "6") int limit) {
        
        List<BudgetResponse> budgets = budgetService.getRecentBudgets(bookId, limit);
        return ResponseEntity.ok(budgets);
    }
} 