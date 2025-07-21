package garabu.garabuServer.dto.recurring;

import garabu.garabuServer.domain.AmountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpcomingTransactionResponse {
    private Long id;
    private String name;
    private AmountType amountType;
    private BigDecimal amount;
    private String categoryName;
    private LocalDate executionDate;
} 