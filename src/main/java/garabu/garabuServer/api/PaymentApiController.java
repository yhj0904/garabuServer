package garabu.garabuServer.api;

import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.service.PaymentService;
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

    private final PaymentService paymentService;

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
