package garabu.garabuServer.dto;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.PaymentMethod;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class LedgerDTO {
    private Long id;
    private Member member;
    private String book;
    private String category;
    private String paymentMethod;
    private LocalDate date;
    private BigDecimal amount;
    private String description;
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
