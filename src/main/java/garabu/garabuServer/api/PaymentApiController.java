package garabu.garabuServer.api;

import garabu.garabuServer.domain.Book;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.dto.PaymentMethodDto;
import garabu.garabuServer.exception.DuplicateResourceException;
import garabu.garabuServer.service.BookService;
import garabu.garabuServer.service.PaymentService;
import garabu.garabuServer.service.UserBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 결제 수단(PaymentMethod) 관리 REST 컨트롤러
 *
 * <p>현금·카드·계좌이체 등 가계부 기록에 사용되는 결제 수단을
 * 생성·조회하는 엔드포인트를 제공합니다.<br/>
 * 모든 API는 JWT Bearer 인증이 필요합니다.</p>
 *
 * @author yhj
 * @version 2.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/payment")          // 공통 prefix
@Tag(name = "Payment", description = "결제 수단 관리 API")
@SecurityRequirement(name = "bearerAuth")   // Swagger UI Authorize 버튼
public class PaymentApiController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentApiController.class);
    
    private final PaymentService paymentService;
    private final BookService bookService;
    private final UserBookService userBookService;

    /* ───────────────────────── 결제 수단 생성 ───────────────────────── */
    /**
     * 새로운 결제 수단을 등록합니다.
     *
     * @param request 결제 수단 생성 요청 DTO
     * @return 생성된 결제 수단 ID
     */
    @PostMapping
    @Operation(
            summary     = "결제 수단 생성",
            description = "새로운 결제 수단을 등록하고 고유 ID를 반환합니다."
    )
    @RequestBody(
            required = true,
            description = "결제 수단 생성 요청 본문",
            content = @Content(
                    schema = @Schema(implementation = CreatePaymentRequest.class),
                    examples = @ExampleObject(
                            name  = "현금 예시",
                            value = "{ \"payment\": \"현금\" }"
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description  = "결제 수단 생성 성공",
                    content      = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @org.springframework.web.bind.annotation.RequestBody CreatePaymentRequest request) {

        PaymentMethod payment = new PaymentMethod();
        payment.setPayment(request.getPayment());

        Long id = paymentService.registPayment(payment);
        return ResponseEntity
                .status(201)
                .body(new CreatePaymentResponse(id));
    }

    /* ───────────────────────── 결제 수단 목록 조회 ───────────────────────── */
    /**
     * 등록된 모든 결제 수단을 조회합니다.
     *
     * @return 결제 수단 리스트
     */
    @GetMapping("/list")
    @Operation(
            summary     = "결제 수단 목록 조회",
            description = "시스템에 등록된 모든 결제 수단을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "결제 수단 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListPaymentResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ListPaymentResponse> listPayments() {

        List<ListPaymentDto> dtoList = paymentService.findAllPayment().stream()
                .map(p -> new ListPaymentDto(p.getId(), p.getPayment()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListPaymentResponse(dtoList));
    }

    /* ───────────────────────── 가계부별 결제 수단 목록 조회 ───────────────────────── */
    /**
     * 특정 가계부의 결제 수단 목록을 조회합니다.
     *
     * @param bookId 가계부 ID
     * @return 가계부별 결제 수단 리스트
     */
    @GetMapping("/book/{bookId}")
    @Operation(
            summary     = "가계부별 결제 수단 목록 조회",
            description = "특정 가계부에 속한 결제 수단 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "가계부별 결제 수단 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListPaymentResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ListPaymentResponse> listPaymentsByBook(@PathVariable Long bookId) {
        try {
            Book book = bookService.findById(bookId);
            
            // 사용자가 해당 가계부에 접근 권한이 있는지 확인
            userBookService.validateBookAccess(book);
            
            // DTO 기반 캐싱된 결과 사용
            List<PaymentMethodDto> payments = paymentService.findByBookDto(book);

            List<ListPaymentDto> dtoList = payments.stream()
                    .map(p -> new ListPaymentDto(p.getId(), p.getPayment()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ListPaymentResponse(dtoList));
        } catch (ClassCastException e) {
            logger.error("Redis 캐시 직렬화 오류 발생: {}", e.getMessage());
            // 캐시 초기화 후 재시도
            paymentService.clearAllPaymentCaches();
            
            Book book = bookService.findById(bookId);
            userBookService.validateBookAccess(book);
            
            List<PaymentMethodDto> payments = paymentService.findByBookDto(book);

            List<ListPaymentDto> dtoList = payments.stream()
                    .map(p -> new ListPaymentDto(p.getId(), p.getPayment()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ListPaymentResponse(dtoList));
        } catch (Exception e) {
            // 로그 출력 후 예외 재던짐
            e.printStackTrace();
            throw e;
        }
    }

    /* ───────────────────────── 가계부별 결제 수단 생성 ───────────────────────── */
    /**
     * 특정 가계부에 새로운 결제 수단을 생성합니다.
     *
     * @param bookId 가계부 ID
     * @param request 결제 수단 생성 요청 DTO
     * @return 생성된 결제 수단 ID
     */
    @PostMapping("/book/{bookId}")
    @Operation(
            summary     = "가계부별 결제 수단 생성",
            description = "특정 가계부에 새로운 결제 수단을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description  = "가계부별 결제 수단 생성 성공",
                    content      = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CreatePaymentResponse> createPaymentForBook(
            @PathVariable Long bookId,
            @Valid @org.springframework.web.bind.annotation.RequestBody CreatePaymentRequest request) {

        Book book = bookService.findById(bookId);
        
        // 가계부 편집 권한 확인
        userBookService.validateBookEditAccess(book);
        
        // 가계부 내 중복 검사
        PaymentMethod existingPayment = paymentService.findByBookAndPayment(book, request.getPayment());
        if (existingPayment != null) {
            throw new DuplicateResourceException("이미 존재하는 결제 수단입니다: " + request.getPayment());
        }

        Long id = paymentService.createPaymentForBook(book, request.getPayment());

        return ResponseEntity
                .status(201)
                .body(new CreatePaymentResponse(id));
    }

    /* ───────────────────────── DTO 정의 ───────────────────────── */
    /** 결제 수단 생성 요청 DTO */
    @Data
    @Schema(description = "결제 수단 생성 요청 DTO")
    static class CreatePaymentRequest {
        @Schema(description = "결제 수단명", example = "현금", requiredMode = Schema.RequiredMode.REQUIRED)
        private String payment;
    }

    /** 결제 수단 생성 응답 DTO */
    @Data
    @Schema(description = "결제 수단 생성 응답 DTO")
    static class CreatePaymentResponse {
        @Schema(description = "생성된 결제 수단 ID", example = "5")
        private Long id;

        public CreatePaymentResponse(Long id) {
            this.id = id;
        }
    }

    /** 결제 수단 목록 응답 DTO */
    @Data
    @Schema(description = "결제 수단 목록 응답 DTO")
    static class ListPaymentResponse {
        @Schema(description = "결제 수단 배열")
        private List<ListPaymentDto> payments;

        public ListPaymentResponse(List<ListPaymentDto> payments) {
            this.payments = payments;
        }
    }

    /** 결제 수단 단건 DTO */
    @Data
    @Schema(description = "결제 수단 단건 DTO")
    static class ListPaymentDto {
        @Schema(description = "결제 수단 ID", example = "5")
        private Long id;

        @Schema(description = "결제 수단명", example = "카드")
        private String payment;

        public ListPaymentDto(Long id, String payment) {
            this.id = id;
            this.payment = payment;
        }
    }
}
