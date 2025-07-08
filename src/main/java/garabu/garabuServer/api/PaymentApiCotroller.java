package garabu.garabuServer.api;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 결제 수단 관리 API 컨트롤러
 * 
 * 가계부 기록의 결제 수단 생성, 조회 등의 기능을 제공합니다.
 * 현금, 카드, 계좌이체 등의 결제 수단을 관리합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 수단 관리 API")
public class PaymentApiCotroller {

    private final PaymentService paymentService;

    /**
     * 새로운 결제 수단을 생성합니다.
     * 
     * @param request 결제 수단 생성 요청 정보
     * @return 생성된 결제 수단의 ID
     */
    @PostMapping("/api/v2/payment")
    @Operation(
        summary = "결제 수단 생성",
        description = "새로운 결제 수단을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "결제 수단 생성 성공",
            content = @Content(schema = @Schema(implementation = CreatePaymentResponse.class))
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
    public CreatePaymentResponse paymentV2(
        @Parameter(description = "결제 수단 생성 정보", required = true)
        @RequestBody @Valid CreatePaymentRequest request){
        
        PaymentMethod payment = new PaymentMethod();
        payment.setPayment(request.getPayment());
        Long id = paymentService.registPayment(payment);
        return new CreatePaymentResponse(id);
    }
    
    /**
     * 모든 결제 수단 목록을 조회합니다.
     * 
     * @return 결제 수단 목록
     */
    @GetMapping("/api/v2/payment/list")
    @Operation(
        summary = "결제 수단 목록 조회",
        description = "시스템에 등록된 모든 결제 수단을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "결제 수단 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ListPaymentResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public ListPaymentResponse listPayments() {
        List<PaymentMethod> payment = paymentService.findAllPayment();
        List<ListPaymentDto> data = payment.stream()
                .map(c -> new ListPaymentDto(c.getId(), c.getPayment()))
                .collect(Collectors.toList());
        return new ListPaymentResponse(data);
    }

    /**
     * 결제 수단 목록 응답 DTO
     */
    @Data
    static class ListPaymentResponse {
        @Parameter(description = "결제 수단 목록")
        private List<PaymentApiCotroller.ListPaymentDto> payments;
        
        public ListPaymentResponse(List<PaymentApiCotroller.ListPaymentDto> payments) {
            this.payments = payments;
        }
    }

    /**
     * 결제 수단 정보 DTO
     */
    @Data
    static class ListPaymentDto {
        @Parameter(description = "결제 수단 ID")
        private Long id;
        
        @Parameter(description = "결제 수단명")
        private String payment;
        
        public ListPaymentDto(Long id, String payment) {
            this.id = id;
            this.payment = payment;
        }
    }

    /**
     * 결제 수단 생성 요청 DTO
     */
    @Data
    static class CreatePaymentRequest {
        @Parameter(description = "결제 수단명", example = "현금", required = true)
        private String payment;
    }
    
    /**
     * 결제 수단 생성 응답 DTO
     */
    @Data
    static class CreatePaymentResponse {
        @Parameter(description = "생성된 결제 수단의 ID")
        private Long id;
        
        public CreatePaymentResponse(Long id) {
            this.id = id;
        }
    }
}
