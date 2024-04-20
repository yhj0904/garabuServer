package garabu.garabuServer.api;

import garabu.garabuServer.domain.*;

import garabu.garabuServer.service.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequiredArgsConstructor
public class LedgerApiController {

    private static final Logger logger = LoggerFactory.getLogger(LedgerApiController.class);

    private final BookService bookService; // 가정: Book 정보를 가져오는 서비스
    private final CategoryService categoryService; // 가정: Category 정보를 가져오는 서비스
    private final PaymentService paymentService;
    private final LedgerService ledgerService;
    private final MemberService memberService;

    @PostMapping("/api/v2/ledger")
    public CreateLedgerResponse saveMemberV2(@RequestBody @Valid CreateLedgerRequest request) {
        try {

            // 로깅: 요청받은 데이터 전체 출력
            logger.info("Received request to save ledger: {}", request);

            // 로깅: 개별 필드 값 출력
            logger.info("Date: {}", request.getDate());
            logger.info("Amount: {}", request.getAmount());
            logger.info("Description: {}", request.getDescription());
            logger.info("Memo: {}", request.getMemo());
            logger.info("Amount Type: {}", request.getAmountType());
            logger.info("Title: {}", request.getTitle());
            logger.info("Payment: {}", request.getPayment());
            logger.info("Category: {}", request.getCategory());
            logger.info("Spender: {}", request.getSpender());
            logger.info("Received request to save ledger: {}", request);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Member currentMember = memberService.findMemberByUsername(authentication.getName());
            logger.info("Current Member: {}", currentMember);

            Ledger ledger = new Ledger();
            ledger.setDate(request.getDate());
            ledger.setAmount(request.getAmount());
            ledger.setDescription(request.getDescription());
            ledger.setMemo(request.getMemo());
            ledger.setAmountType(request.getAmountType());
            ledger.setMember(currentMember);
            ledger.setSpender(request.getSpender());

            Book book = bookService.findByTitle(request.getTitle());
            ledger.setBook(book);

            PaymentMethod paymentMethod = paymentService.findByPayment(request.getPayment());
            ledger.setPaymentMethod(paymentMethod);

            ledger.setCategory(categoryService.findByCategory(request.getCategory()));
            Long id = ledgerService.registLedger(ledger);
            logger.info("Ledger registered with id: {}", id);

            return new CreateLedgerResponse(id);
        } catch (Exception e) {
            logger.error("Error saving ledger: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving ledger", e);
        }
    }


    @PostMapping("/api/v2/ledger/list")
    public List<Ledger> getAllLedgers() {
        return ledgerService.findAllLedgers();
    }

    @Data
    static class CreateLedgerRequest {
        private LocalDate date;
        private Integer amount;
        private String description;
        private String memo;
        private AmountType amountType;
        private String title;  // Title of the book
        private String payment;  // Payment method description
        private String category;  // Category description
        private String spender;

    }
    @Data
    static class CreateLedgerResponse {
        private Long id;
        public CreateLedgerResponse(Long id) {
            this.id = id;
        }
    }
}
