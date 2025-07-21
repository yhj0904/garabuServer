package garabu.garabuServer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Ledger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 가계부 기록 정보 DTO
 *
 * 가계부 기록의 주요 정보를 담는 데이터 전송 객체입니다.
 * LazyInitializationException 방지를 위해 엔티티 대신 DTO 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "가계부 기록 정보 DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LedgerDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "기록 ID")
    private Long id;
    
    @Schema(description = "작성자 정보")
    private SimpleMemberDTO member;
    
    @Schema(description = "가계부 정보")
    private SimpleBookDTO book;
    
    @Schema(description = "카테고리 정보")
    private SimpleCategoryDTO category;
    
    @Schema(description = "결제수단 정보")
    private SimplePaymentMethodDTO paymentMethod;
    
    @Schema(description = "금액 유형", example = "EXPENSE")
    private AmountType amountType;
    
    @Schema(description = "자금 사용자", example = "홍길동")
    private String spender;
    
    @Schema(description = "기록 날짜", example = "2024-01-15")
    private LocalDate date;
    
    @Schema(description = "금액", example = "50000")
    private Integer amount;
    
    @Schema(description = "상세 내용", example = "월급")
    private String description;
    
    @Schema(description = "메모", example = "1월 월급")
    private String memo;
    
    /**
     * 간단한 회원 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "간단한 회원 정보")
    public static class SimpleMemberDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "회원 ID", example = "1")
        private Long id;
        
        @Schema(description = "이름", example = "홍길동")
        private String name;
        
        @Schema(description = "이메일", example = "user@example.com")
        private String email;
    }
    
    /**
     * 간단한 가계부 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "간단한 가계부 정보")
    public static class SimpleBookDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "가계부 ID", example = "1")
        private Long id;
        
        @Schema(description = "가계부 이름", example = "개인 가계부")
        private String title;
    }
    
    /**
     * 간단한 카테고리 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "간단한 카테고리 정보")
    public static class SimpleCategoryDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "카테고리 ID", example = "1")
        private Long id;
        
        @Schema(description = "카테고리 이름", example = "식비")
        private String category;
        
        @Schema(description = "카테고리 이모지", example = "🍔")
        private String emoji;
        
        @Schema(description = "기본 제공 카테고리 여부", example = "false")
        private Boolean isDefault;
    }
    
    /**
     * 간단한 결제수단 정보 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "간단한 결제수단 정보")
    public static class SimplePaymentMethodDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Schema(description = "결제수단 ID", example = "1")
        private Long id;
        
        @Schema(description = "결제수단명", example = "현금")
        private String payment;
    }
    
    /**
     * Ledger 엔티티를 LedgerDTO로 변환
     */
    public static LedgerDTO from(Ledger ledger) {
        if (ledger == null) return null;
        
        return LedgerDTO.builder()
                .id(ledger.getId())
                .member(ledger.getMember() != null ? SimpleMemberDTO.builder()
                        .id(ledger.getMember().getId())
                        .name(ledger.getMember().getName())
                        .email(ledger.getMember().getEmail())
                        .build() : null)
                .book(ledger.getBook() != null ? SimpleBookDTO.builder()
                        .id(ledger.getBook().getId())
                        .title(ledger.getBook().getTitle())
                        .build() : null)
                .category(ledger.getCategory() != null ? SimpleCategoryDTO.builder()
                        .id(ledger.getCategory().getId())
                        .category(ledger.getCategory().getCategory())
                        .emoji(ledger.getCategory().getEmoji())
                        .isDefault(ledger.getCategory().getIsDefault())
                        .build() : null)
                .paymentMethod(ledger.getPaymentMethod() != null ? SimplePaymentMethodDTO.builder()
                        .id(ledger.getPaymentMethod().getId())
                        .payment(ledger.getPaymentMethod().getPayment())
                        .build() : null)
                .amountType(ledger.getAmountType())
                .spender(ledger.getSpender())
                .date(ledger.getDate())
                .amount(ledger.getAmount())
                .description(ledger.getDescription())
                .memo(ledger.getMemo())
                .build();
    }
    
    /**
     * Ledger 엔티티 리스트를 LedgerDTO 리스트로 변환
     */
    public static List<LedgerDTO> from(List<Ledger> ledgers) {
        if (ledgers == null) return List.of();
        
        return ledgers.stream()
                .map(LedgerDTO::from)
                .collect(Collectors.toList());
    }
    
    // 기존 생성자 유지 (호환성)
    public LedgerDTO(LocalDate date, BigDecimal amount, String description, String memo, String category, String book, String paymentMethod) {
        this.date = date;
        this.amount = amount != null ? amount.intValue() : null;
        this.description = description;
        this.memo = memo;
        this.category = SimpleCategoryDTO.builder().category(category).build();
        this.book = SimpleBookDTO.builder().title(book).build();
        this.paymentMethod = SimplePaymentMethodDTO.builder().payment(paymentMethod).build();
    }
}
