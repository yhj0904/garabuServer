package garabu.garabuServer.api;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.LedgerDTO;
import garabu.garabuServer.dto.LedgerSearchConditionDTO;
import garabu.garabuServer.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 가계부 기록(Ledger) 관리 REST 컨트롤러
 *
 * <p>수입(INCOME)‧지출(EXPENSE)‧이체(TRANSFER) 등
 * 다양한 금융 기록을 생성·조회하는 엔드포인트를 제공합니다.<br/>
 * 모든 요청은 **JWT Bearer 토큰** 인증을 요구합니다.</p>
 *
 * @author yhj
 * @version 2.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/ledger")          // 공통 prefix
@Tag(name = "Ledger", description = "가계부 기록 관리 API")
@SecurityRequirement(name = "bearerAuth")  // Swagger UI Authorize 버튼
public class LedgerApiController {

    private static final Logger logger = LoggerFactory.getLogger(LedgerApiController.class);

    private final BookService    bookService;
    private final CategoryService categoryService;
    private final PaymentService  paymentService;
    private final LedgerService   ledgerService;
    private final MemberService   memberService;
    private final UserBookService userBookService;

    // ───────────────────────── 기록 생성 ─────────────────────────
    /**
     * 새로운 가계부 기록을 생성합니다.
     *
     * @param request Ledger 생성 요청 DTO
     * @return 생성된 Ledger ID
     */
    @PostMapping("/ledgers")
    @Transactional
    @Operation(
            summary     = "가계부 기록 생성",
            description = "새로운 수입/지출 기록을 추가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description  = "기록 생성 성공",
                    content      = @Content(schema = @Schema(implementation = CreateLedgerResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (VIEWER는 작성 불가)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CreateLedgerResponse> createLedger(
            @Valid @RequestBody CreateLedgerRequest request
    ) {

        try {
            /* ────── 1. 로그인 사용자 확인 ────── */
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Member currentMember = memberService.findMemberByUsername(auth.getName());

            /* ────── 2. 가계부 권한 확인 ────── */
            Book book = bookService.findByTitle(request.getTitle());
            
            // 사용자의 가계부 권한 확인
            UserBook userBook = userBookService.findByBookIdAndMemberId(book.getId(), currentMember.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 가계부에 접근 권한이 없습니다."));
            
            // VIEWER는 기록 작성 불가
            if (userBook.getBookRole() == BookRole.VIEWER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "조회 권한만 있습니다. 기록을 작성할 수 없습니다.");
            }

            /* ────── 3. 엔티티 매핑 ────── */
            Ledger ledger = new Ledger();
            ledger.setDate(request.getDate());
            ledger.setAmount(request.getAmount());
            ledger.setDescription(request.getDescription());
            ledger.setMemo(request.getMemo());
            ledger.setAmountType(request.getAmountType());
            ledger.setMember(currentMember);
            ledger.setSpender(request.getSpender());

            ledger.setBook(book);

            PaymentMethod paymentMethod = paymentService.findByPayment(request.getPayment());
            ledger.setPaymentMethod(paymentMethod);

            Category category = categoryService.findByCategory(request.getCategory());
            ledger.setCategory(category);

            /* ────── 4. 저장 ────── */
            Long id = ledgerService.registLedger(ledger);
            logger.info("Ledger registered with id={}", id);

            return ResponseEntity
                    .status(201)
                    .body(new CreateLedgerResponse(id));

        } catch (Exception e) {
            logger.error("Error saving ledger", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving ledger", e);
        }
    }

    // ───────────────────────── 기록 목록 조회 ─────────────────────────
    /**
     * 시스템에 등록된 모든 가계부 기록을 조회합니다.
     *
     * @return Ledger 리스트
     */
    @GetMapping("/list")
    @Operation(
            summary     = "가계부 기록 목록 조회",
            description = """
        날짜 범위·카테고리·금액 유형 등으로 필터링하고
        페이지네이션 / 정렬이 가능한 엔드포인트입니다.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListLedgerResponse.class)))
    })
    public ResponseEntity<ListLedgerResponse> listLedgers(
            @Parameter(description = "시작 날짜(yyyy-MM-dd)", example = "2025-07-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "종료 날짜(yyyy-MM-dd)", example = "2025-07-31")
            @RequestParam(required = false) LocalDate endDate,

            @Parameter(description = "금액 유형(INCOME/EXPENSE/TRANSFER)",
                    example = "EXPENSE",
                    schema = @Schema(allowableValues = {"INCOME","EXPENSE","TRANSFER"}))
            @RequestParam(required = false) AmountType amountType,

            @Parameter(description = "카테고리명", example = "식비")           @RequestParam(required = false) String category,

            @Parameter(description = "결제 수단", example = "카드")           @RequestParam(required = false) String payment,

            @ParameterObject Pageable pageable                                 // page, size, sort
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = memberService.findMemberByUsername(auth.getName()).getId();

        LedgerSearchConditionDTO cond = new LedgerSearchConditionDTO(
                memberId, startDate, endDate, amountType, category, payment
        );

        Page<Ledger> page = ledgerService.search(cond, pageable);

        List<LedgerDto> dtoList = page.getContent().stream()
                .map(LedgerDto::from)
                .toList();

        return ResponseEntity.ok(new ListLedgerResponse(dtoList, page.getTotalElements()));
    }


    // ───────────────────────── DTO 정의 ─────────────────────────
    /** Ledger 생성 요청 DTO */
    @Data
    @Schema(description = "가계부 기록 생성 요청 DTO")
    static class CreateLedgerRequest {
        @Schema(description = "기록 날짜", example = "2025-07-08", requiredMode = Schema.RequiredMode.REQUIRED)
        private LocalDate date;

        @Schema(description = "금액(원)", example = "3000000", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer amount;

        @Schema(description = "상세 내용", example = "7월 월급")
        private String description;

        @Schema(description = "메모", example = "세후 지급액")
        private String memo;

        @Schema(description = "금액 유형(INCOME/EXPENSE/TRANSFER)",
                example = "INCOME",
                allowableValues = {"INCOME", "EXPENSE", "TRANSFER"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        private AmountType amountType;

        @Schema(description = "가계부 제목", example = "가족 가계부", requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;

        @Schema(description = "결제 수단", example = "이체", requiredMode = Schema.RequiredMode.REQUIRED)
        private String payment;

        @Schema(description = "카테고리명", example = "급여", requiredMode = Schema.RequiredMode.REQUIRED)
        private String category;

        @Schema(description = "지출자/수입원", example = "회사")
        private String spender;
    }

    /** Ledger 생성 응답 DTO */
    @Data
    @Schema(description = "가계부 기록 생성 응답 DTO")
    static class CreateLedgerResponse {
        @Schema(description = "생성된 Ledger ID", example = "101")
        private Long id;

        public CreateLedgerResponse(Long id) {
            this.id = id;
        }
    }

    /** Ledger 목록 응답 DTO */
    @Data
    @Schema(description = "가계부 기록 목록 응답 DTO")
    static class ListLedgerResponse {
        @Schema(description = "Ledger 배열")
        private List<LedgerDto> ledgers;

        public ListLedgerResponse(List<LedgerDto> ledgers, long totalElements) {
            this.ledgers = ledgers;
        }
    }

    /** Ledger 요약 DTO */
    @Data
    @Schema(description = "Ledger 요약 DTO")
    static class LedgerDto {
        @Schema(description = "Ledger ID", example = "101")
        private Long id;

        @Schema(description = "날짜", example = "2025-07-08")
        private LocalDate date;

        @Schema(description = "금액(원)", example = "3000000")
        private Integer amount;

        @Schema(description = "카테고리명", example = "급여")
        private String category;

        @Schema(description = "금액 유형", example = "INCOME")
        private AmountType amountType;

        public static LedgerDto from(Ledger ledger) {
            LedgerDto dto = new LedgerDto();
            dto.id         = ledger.getId();
            dto.date       = ledger.getDate();
            dto.amount     = ledger.getAmount();
            dto.category   = ledger.getCategory().getCategory();
            dto.amountType = ledger.getAmountType();
            return dto;
        }
    }

    @Data
    @AllArgsConstructor
    public class LedgerSearchCondition {
        private LocalDate startDate;
        private LocalDate endDate;
        private AmountType amountType;
        private String category;
        private String payment;
    }

}
