package garabu.garabuServer.dto;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 가계부 기록 정보 DTO
 *
 * 가계부 기록의 주요 정보를 담는 데이터 전송 객체입니다.
 */
@Getter
@Schema(description = "가계부 기록 정보 DTO")
public class LedgerDTO {
    @Schema(description = "기록 ID")
    private Long id;
    @Schema(description = "회원 정보")
    private Member member;
    @Schema(description = "가계부 이름")
    private String book;
    @Schema(description = "카테고리명")
    private String category;
    @Schema(description = "결제수단명")
    private String paymentMethod;
    @Schema(description = "기록 날짜", example = "2024-01-15")
    private LocalDate date;
    @Schema(description = "금액", example = "50000")
    private BigDecimal amount;
    @Schema(description = "상세 내용", example = "월급")
    private String description;
    @Schema(description = "메모", example = "1월 월급")
    private String memo;

    public LedgerDTO(LocalDate date, BigDecimal amount, String description, String memo, String category, String book, String paymentMethod) {
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.memo = memo;
        this.category = category;
        this.book = book;
        this.paymentMethod = paymentMethod;
    }
}
