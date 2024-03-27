package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class Ledger {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long id;        //기록의 고유 식별자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  //가계부 기록자의 고유 식별자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "book_id")
    private Book book;      // 가계부의 고유 식별자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리 고유 식별자

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentMethod paymentMethod;    //결제수단의 ID


    private LocalDate date;  // 날짜
    private BigDecimal amount;  // 금액
    private String description; //상세내용
    private String memo;            // 메모

}
