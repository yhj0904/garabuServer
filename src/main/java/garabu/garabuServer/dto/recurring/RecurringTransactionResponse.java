package garabu.garabuServer.dto.recurring;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.RecurrenceType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecurringTransactionResponse {
    private Long id;
    private String name;
    private String description;
    private AmountType amountType;
    private BigDecimal amount;
    private Long categoryId;
    private String categoryName;
    private RecurrenceType recurrenceType;
    private Integer recurrenceInterval;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private Boolean isActive;
    private Integer executionCount;
    private LocalDateTime createdAt;
} 