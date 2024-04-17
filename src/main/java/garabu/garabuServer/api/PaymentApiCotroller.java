package garabu.garabuServer.api;

import garabu.garabuServer.domain.AmountType;
import garabu.garabuServer.domain.Category;
import garabu.garabuServer.domain.PaymentMethod;
import garabu.garabuServer.service.PaymentService;
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

@RestController
@RequiredArgsConstructor

public class PaymentApiCotroller {

    private final PaymentService paymentService;

    @PostMapping("/api/v2/payment")
    public CreatePaymentResponse paymentV2(@RequestBody @Valid
                                           CreatePaymentRequest request){
        PaymentMethod payment = new PaymentMethod();
        payment.setPayment(request.getPayment());
        Long id = paymentService.registPayment(payment);
        return new CreatePaymentResponse(id);

    }
    @GetMapping("/api/v2/payment/list")
    public ListPaymentResponse listPayments() {
        List<PaymentMethod> payment = paymentService.findAllPayment();
        List<ListPaymentDto> data = payment.stream()
                .map(c -> new ListPaymentDto(c.getId(), c.getPayment()))
                .collect(Collectors.toList());
        return new ListPaymentResponse(data);
    }

    @Data
    static class ListPaymentResponse {
        private List<PaymentApiCotroller.ListPaymentDto> payments;
        public ListPaymentResponse(List<PaymentApiCotroller.ListPaymentDto> payments) {
            this.payments = payments;
        }
    }

    @Data
    static class ListPaymentDto {
        private Long id;
        private String payment;
        public ListPaymentDto(Long id, String payment) {
            this.id = id;
            this.payment = payment;
        }
    }



    @Data
    static class CreatePaymentRequest {

        private String payment;

    }
    @Data
    static class CreatePaymentResponse {
        private Long id;
        public CreatePaymentResponse(Long id) {
            this.id = id;
        }
    }
}
