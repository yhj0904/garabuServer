package garabu.garabuServer.dto;

import garabu.garabuServer.domain.AmountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 가계부 기록 생성 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "가계부 기록 생성 응답 DTO")
public class CreateLedgerResponse {
    
    @Schema(description = "생성된 Ledger ID", example = "101")
    private Long id;
    
    @Schema(description = "기록 날짜", example = "2025-07-08")
    private LocalDate date;
    
    @Schema(description = "금액(원)", example = "3000000")
    private Long amount;
    
    @Schema(description = "상세 내용", example = "7월 월급")
    private String description;
    
    @Schema(description = "금액 유형", example = "INCOME")
    private AmountType amountType;
    
    @Schema(description = "카테고리명", example = "급여")
    private String category;
    
    @Schema(description = "결제 수단", example = "이체")
    private String payment;
} 