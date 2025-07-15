package garabu.garabuServer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 예산 요약 응답 DTO (실제 수입/지출과 비교)
 */
@Data
@Schema(description = "예산 요약 응답 DTO")
public class BudgetSummaryResponse {

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

    @Schema(description = "실제 수입", example = "4500000")
    private Integer actualIncome;

    @Schema(description = "실제 지출", example = "2800000")
    private Integer actualExpense;

    @Schema(description = "수입 달성률", example = "90.0")
    private Double incomeAchievementRate;

    @Schema(description = "지출 달성률", example = "93.3")
    private Double expenseAchievementRate;

    @Schema(description = "수입 예산 대비 초과/미달", example = "-500000")
    private Integer incomeDifference;

    @Schema(description = "지출 예산 대비 초과/미달", example = "-200000")
    private Integer expenseDifference;

    @Schema(description = "예산 메모", example = "1월 예산")
    private String memo;

    @Schema(description = "생성일")
    private String createdAt;

    @Schema(description = "수정일")
    private String updatedAt;
} 