package garabu.garabuServer.dto.tag;

import garabu.garabuServer.domain.AmountType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionSummary {
    private Long id;
    private LocalDate date;
    private Long amount;
    private String description;
    private AmountType amountType;
    private String categoryName;
} 