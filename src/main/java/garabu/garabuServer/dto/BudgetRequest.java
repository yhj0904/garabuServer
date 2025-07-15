package garabu.garabuServer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 예산 생성/수정 요청 DTO
 */
@Data
@Schema(description = "예산 생성/수정 요청 DTO")
public class BudgetRequest {

    @NotBlank(message = "예산 년월은 필수입니다")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "예산 년월은 YYYY-MM 형식이어야 합니다")
    @Schema(description = "예산 년월", example = "2025-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String budgetMonth;

    @Schema(description = "수입 예산", example = "5000000")
    private Integer incomeBudget;

    @Schema(description = "지출 예산", example = "3000000")
    private Integer expenseBudget;

    @Schema(description = "예산 메모", example = "1월 예산")
    private String memo;
} 