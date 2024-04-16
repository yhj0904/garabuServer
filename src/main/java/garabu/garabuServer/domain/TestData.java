package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class TestData {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;  // 날짜

    private String amountType;      // 분류 유형 수입 지출 이체

    private String member;  //가계부 기록자의 고유 식별자

    private String user; // 자금 사용자

    private String category; // 카테고리 고유 식별자

    private String paymentMethod;    //결제수단의 ID

    private String memo;            // 메모

    private BigDecimal amount;  // 금액
}
