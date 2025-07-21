package garabu.garabuServer.dto.recurring;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecurringExecutionResponse {
    private Long recurringTransactionId;
    private Long ledgerId;
    private LocalDate executionDate;
    private BigDecimal amount;
    private String message;
} 