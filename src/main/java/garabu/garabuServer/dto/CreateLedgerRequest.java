package garabu.garabuServer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import garabu.garabuServer.domain.AmountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 가계부 기록 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "가계부 기록 생성 요청 DTO")
public class CreateLedgerRequest {
    
    @NotNull(message = "기록 날짜는 필수입니다")
    @PastOrPresent(message = "기록 날짜는 현재 또는 과거 날짜여야 합니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("date")
    @Schema(description = "기록 날짜", example = "2025-07-08", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 0보다 큰 값이어야 합니다")
    @JsonProperty("amount")
    @Schema(description = "금액(원)", example = "3000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer amount;

    @NotBlank(message = "상세 내용은 필수입니다")
    @JsonProperty("description")
    @Schema(description = "상세 내용", example = "7월 월급", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @JsonProperty("memo")
    @Schema(description = "메모", example = "세후 지급액")
    private String memo;

    @NotNull(message = "금액 유형은 필수입니다")
    @JsonProperty("amountType")
    @Schema(description = "금액 유형(INCOME/EXPENSE/TRANSFER)",
            example = "INCOME",
            allowableValues = {"INCOME", "EXPENSE", "TRANSFER"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AmountType amountType;

    @NotNull(message = "가계부 ID는 필수입니다")
    @Schema(description = "가계부 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("bookId")
    private Long bookId;

    @NotBlank(message = "결제 수단은 필수입니다")
    @JsonProperty("payment")
    @Schema(description = "결제 수단", example = "이체", requiredMode = Schema.RequiredMode.REQUIRED)
    private String payment;

    @NotBlank(message = "카테고리명은 필수입니다")
    @JsonProperty("category")
    @Schema(description = "카테고리명", example = "급여", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;

    @JsonProperty("spender")
    @Schema(description = "지출자/수입원", example = "회사")
    private String spender;
} 