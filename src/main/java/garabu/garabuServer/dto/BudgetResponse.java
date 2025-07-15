package garabu.garabuServer.dto;

import garabu.garabuServer.domain.Budget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 예산 조회 응답 DTO
 */
@Data
@Schema(description = "예산 조회 응답 DTO")
public class BudgetResponse {

    @Schema(description = "예산 ID", example = "1")
    private Long id;

    @Schema(description = "가계부 ID", example = "1")
    private Long bookId;

    @Schema(description = "예산 년월", example = "2025-01")
    private String budgetMonth;

    @Schema(description = "수입 예산", example = "5000000")
    private Integer incomeBudget;

    @Schema(description = "지출 예산", example = "3000000")
    private Integer expenseBudget;

    @Schema(description = "예산 메모", example = "1월 예산")
    private String memo;

    @Schema(description = "생성일")
    private LocalDate createdAt;

    @Schema(description = "수정일")
    private LocalDate updatedAt;

    public static BudgetResponse from(Budget budget) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setBookId(budget.getBook().getId());
        response.setBudgetMonth(budget.getBudgetMonth());
        response.setIncomeBudget(budget.getIncomeBudget());
        response.setExpenseBudget(budget.getExpenseBudget());
        response.setMemo(budget.getMemo());
        response.setCreatedAt(budget.getCreatedAt());
        response.setUpdatedAt(budget.getUpdatedAt());
        return response;
    }
} 