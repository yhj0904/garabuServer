package garabu.garabuServer.dto;

import garabu.garabuServer.domain.AmountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.time.LocalDate;

/**
 * Ledger(가계부 기록) 조회 시 사용하는
 * 검색 조건 DTO 입니다.
 *
 * <p>날짜 범위·금액 유형·카테고리·결제 수단 등을
 * 복합적으로 전달해 MyBatis 동적 SQL에서 WHERE 절을
 * 구성할 때 사용합니다.</p>
 */
@Data
@Alias("LedgerSearchCondition")   // MyBatis typeAlias
@Schema(name = "LedgerSearchCondition",
        description = "가계부 기록 검색 조건 DTO")
public class LedgerSearchConditionDTO {

    @Schema(description = "로그인한 회원 ID(내부 주입용)",
            example = "15",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long memberId;

    @Schema(description = "검색 시작 날짜",
            example = "2025-07-01",
            type = "string",
            format = "date")
    private LocalDate startDate;

    @Schema(description = "검색 종료 날짜",
            example = "2025-07-31",
            type = "string",
            format = "date")
    private LocalDate endDate;

    @Schema(description = "금액 유형",
            example = "EXPENSE",
            allowableValues = {"INCOME", "EXPENSE", "TRANSFER"})
    private AmountType amountType;

    @Schema(description = "카테고리명",
            example = "식비")
    private String category;

    @Schema(description = "결제 수단",
            example = "카드")
    private String payment;

    public LedgerSearchConditionDTO(Long memberId,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    AmountType amountType,
                                    String category,
                                    String payment) {
        this.memberId   = memberId;
        this.startDate  = startDate;
        this.endDate    = endDate;
        this.amountType = amountType;
        this.category   = category;
        this.payment    = payment;
    }
}
