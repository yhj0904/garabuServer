package garabu.garabuServer.api;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.repository.LedgerJpaRepository;
import garabu.garabuServer.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class LedgerApiController {
    private final LedgerJpaRepository ledgerJpaRepository;
    private final BookService bookService; // 가정: Book 정보를 가져오는 서비스
    private final CategoryService categoryService; // 가정: Category 정보를 가져오는 서비스
    private final PaymentService paymentService;
    private final LedgerService ledgerService;
    private final MemberService memberService;

    @PostMapping("/api/v2/ledger")
    public CreateLedgerResponse saveMemberV2(@RequestBody @Valid
                                                 CreateLedgerRequest request) {
        Ledger ledger = new Ledger();
        ledger.setDate(request.getDate());
        ledger.setAmount(request.getAmount());
        ledger.setDescription(request.getDescription());
        ledger.setMemo(request.getMemo());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member currentMember = memberService.findMemberByUsername(authentication.getName());
        ledger.setMember(currentMember);

        Book book = bookService.findById(request.getBookId());
        ledger.setBook(book);

        PaymentMethod paymentMethod = paymentService.findById(request.getPaymentId());
        ledger.setPaymentMethod(paymentMethod);



        Category category = new Category();
        category.setAmountType(request.getAmountType());
        ledger.setCategory(categoryService.findById(request.getCategoryId()));
        Long id = ledgerService.registLedger(ledger);

        return new CreateLedgerResponse(id);
    }
    @Data
    static class CreateLedgerRequest {
        private LocalDate date;
        private BigDecimal amount;
        private String description;
        private String memo;
        private AmountType amountType;
        private Long bookId; // Assuming IDs are passed
        private Long paymentId;
        private Long categoryId;
    }
    @Data
    static class CreateLedgerResponse {
        private Long id;
        public CreateLedgerResponse(Long id) {
            this.id = id;
        }
    }
}
