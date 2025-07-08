package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

/**
 * 가계부 기록 엔티티 클래스
 * 
 * 사용자의 수입, 지출, 이체 등의 금융 기록을 관리합니다.
 * 회원, 가계부, 카테고리, 결제수단과 연관 관계를 가집니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@Entity
@Getter @Setter
public class Ledger {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long id;        //기록의 고유 식별자

    /**
     * 가계부 기록 작성자
     * ManyToOne 관계로 여러 기록이 한 회원에게 속할 수 있음
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  //가계부 기록자의 고유 식별자

    /**
     * 가계부
     * ManyToOne 관계로 여러 기록이 한 가계부에 속할 수 있음
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "title_id")
    private Book book;      // 가계부의 고유 식별자

    /**
     * 카테고리
     * ManyToOne 관계로 여러 기록이 한 카테고리에 속할 수 있음
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리 고유 식별자

    /**
     * 결제 수단
     * ManyToOne 관계로 여러 기록이 한 결제수단에 속할 수 있음
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentMethod paymentMethod;    //결제수단의 ID

    /**
     * 금액 유형 (수입/지출/이체)
     */
    @Enumerated(EnumType.STRING)
    private AmountType amountType;      // 분류 유형 수입 지출 이체

    //add for user
    private String spender; // 자금 사용자
    private LocalDate date;  // 날짜
    private Integer amount;  // 금액
    private String description; //상세내용
    private String memo;            // 메모

}
