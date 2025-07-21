package garabu.garabuServer.dto.recurring;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateRecurringTransactionRequest {
    
    private String name;
    
    private String description;
    
    @Positive(message = "금액은 양수여야 합니다")
    private BigDecimal amount;
    
    @Positive(message = "반복 간격은 양수여야 합니다")
    private Integer recurrenceInterval;
    
    private LocalDate endDate;
    
    private Boolean autoCreate;
} 