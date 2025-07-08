package garabu.garabuServer.dto;

import garabu.garabuServer.domain.AmountType;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;

@Data
@Alias("LedgerSearchCondition")
public class LedgerSearchConditionDTO {
    private Long memberId;
    private LocalDate startDate;
    private LocalDate endDate;
    private AmountType amountType;
    private String category;
    private String payment;

    public LedgerSearchConditionDTO(Long memberId, LocalDate startDate, LocalDate endDate, AmountType amountType, String category, String payment) {
        this.memberId = memberId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amountType = amountType;
        this.category = category;
        this.payment = payment;
    }
}
