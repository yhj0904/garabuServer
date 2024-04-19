package garabu.garabuServer.api;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.LedgerDTO;
import garabu.garabuServer.repository.LedgerJpaRepository;
import garabu.garabuServer.service.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class LedgerApiController {

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
        ledger.setAmountType(request.getAmountType());


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member currentMember = memberService.findMemberByUsername(authentication.getName());
        ledger.setMember(currentMember);

        Book book = bookService.findByTitle(request.getTitle());
        ledger.setBook(book);

        PaymentMethod paymentMethod = paymentService.findByPayment(request.getPayment());
        ledger.setPaymentMethod(paymentMethod);

        ledger.setCategory(categoryService.findByCategory(request.getCategory()));
        Long id = ledgerService.registLedger(ledger);

        return new CreateLedgerResponse(id);
    }

    @PostMapping("/api/v2/ledger/list")
    public List<Ledger> getAllLedgers() {
        return ledgerService.findAllLedgers();
    }



    @Data
    static class loadingBaseLedgerData{
        private String username;
        private Category category;
        private PaymentMethod payment;
        private Book book;
    }

    @Data
    static class VaildLoginUser{
        private String username;
        private String email;
    }

    @Data
    static class CreateLedgerRequest {
        private LocalDate date;
        private Integer amount;
        private String description;
        private String memo;
        private AmountType amountType;
        private String title; // Assuming IDs are passed
        private String payment;
        private String category;
    }
    @Data
    static class CreateLedgerResponse {
        private Long id;
        public CreateLedgerResponse(Long id) {
            this.id = id;
        }
    }
}
