package garabu.garabuServer.dto.recurring;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.RecurrenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateRecurringTransactionRequest {
    
    @NotBlank(message = "반복 거래 이름은 필수입니다")
    private String name;
    
    private String description;
    
    @NotNull(message = "거래 유형은 필수입니다")
    private AmountType amountType;
    
    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 양수여야 합니다")
    private BigDecimal amount;
    
    private Long categoryId;
    
    private Long paymentMethodId;
    
    private Long assetId;
    
    @NotNull(message = "반복 유형은 필수입니다")
    private RecurrenceType recurrenceType;
    
    @NotNull(message = "반복 간격은 필수입니다")
    @Positive(message = "반복 간격은 양수여야 합니다")
    private Integer recurrenceInterval = 1;
    
    private String recurrenceDay; // 요일(WEEKLY) 또는 날짜(MONTHLY)
    
    @NotNull(message = "시작일은 필수입니다")
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private Integer maxExecutions;
    
    private Boolean autoCreate = true;
} 