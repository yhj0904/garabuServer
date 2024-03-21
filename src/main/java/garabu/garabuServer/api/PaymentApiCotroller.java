package garabu.garabuServer.api;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor

public class PaymentApiCotroller {

    private final PaymentService paymentService;

    @PostMapping("/api/v2/payment")
    public CreatePaymentResponse paymentV2(@RequestBody @Valid
                                           CreatePaymentRequest request){
        PaymentMethod payment = new PaymentMethod();
        payment.setPaymentName(request.getPaymentName());
        Long id = paymentService.registPayment(payment);
        return new CreatePaymentResponse(id);

    }

    @Data
    static class CreatePaymentRequest {
        @Email
        private String paymentName;

    }
    @Data
    static class CreatePaymentResponse {
        private Long id;
        public CreatePaymentResponse(Long id) {
            this.id = id;
        }
    }
}
