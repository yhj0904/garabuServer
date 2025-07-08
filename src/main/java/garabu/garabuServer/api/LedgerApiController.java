package garabu.garabuServer.api;

import garabu.garabuServer.domain.*;

import garabu.garabuServer.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * 가계부 기록 관리 API 컨트롤러
 * 
 * 가계부 기록의 생성, 조회 등의 기능을 제공합니다.
 * 수입, 지출, 이체 등의 금융 기록을 관리합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "가계부 기록 관리 API")
public class LedgerApiController {

    private static final Logger logger = LoggerFactory.getLogger(LedgerApiController.class);

    private final BookService bookService; // 가정: Book 정보를 가져오는 서비스
    private final CategoryService categoryService; // 가정: Category 정보를 가져오는 서비스
    private final PaymentService paymentService;
    private final LedgerService ledgerService;
    private final MemberService memberService;

    /**
     * 새로운 가계부 기록을 생성합니다.
     * 
     * @param request 가계부 기록 생성 요청 정보
     * @return 생성된 가계부 기록의 ID
     */
    @PostMapping("/api/v2/ledger")
    @Operation(
        summary = "가계부 기록 생성",
        description = "새로운 가계부 기록(수입/지출/이체)을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "가계부 기록 생성 성공",
            content = @Content(schema = @Schema(implementation = CreateLedgerResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public CreateLedgerResponse saveMemberV2(
        @Parameter(description = "가계부 기록 정보", required = true)
        @RequestBody @Valid CreateLedgerRequest request) {
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

    /**
     * 모든 가계부 기록을 조회합니다.
     * 
     * @return 가계부 기록 목록
     */
    @PostMapping("/api/v2/ledger/list")
    @Operation(
        summary = "가계부 기록 목록 조회",
        description = "시스템에 등록된 모든 가계부 기록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "가계부 기록 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = Ledger.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public List<Ledger> getAllLedgers() {
        return ledgerService.findAllLedgers();
    }

    /**
     * 가계부 기록 생성 요청 DTO
     */
    @Data
    static class CreateLedgerRequest {
        @Parameter(description = "기록 날짜", example = "2024-01-15")
        private LocalDate date;
        
        @Parameter(description = "금액", example = "50000")
        private Integer amount;
        
        @Parameter(description = "상세 내용", example = "월급")
        private String description;
        
        @Parameter(description = "메모", example = "1월 월급")
        private String memo;
        
        @Parameter(description = "금액 유형 (수입/지출/이체)", example = "INCOME")
        private AmountType amountType;
        
        @Parameter(description = "가계부 제목", example = "내 가계부")
        private String title;  // Title of the book
        
        @Parameter(description = "결제 수단", example = "현금")
        private String payment;  // Payment method description
        
        @Parameter(description = "카테고리", example = "급여")
        private String category;  // Category description
        
        @Parameter(description = "지출자", example = "홍길동")
        private String spender;
    }
    
    /**
     * 가계부 기록 생성 응답 DTO
     */
    @Data
    static class CreateLedgerResponse {
        @Parameter(description = "생성된 가계부 기록의 ID")
        private Long id;
        
        public CreateLedgerResponse(Long id) {
            this.id = id;
        }
    }
}
