package garabu.garabuServer.api;

import garabu.garabuServer.domain.Ledger;
import garabu.garabuServer.service.LedgerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class LedgerApiController {
    private final LedgerService ledgerService;

    @PostMapping("/api/v2/members")
    public CreateLedgerResponse saveMemberV2(@RequestBody @Valid
                                                 CreateLedgerRequest request) {
        Ledger ledger = new Ledger();
        ledger.setDate(request.getDate());
        ledger.setAmount(request.getAmount());
        ledger.setDescription(request.getDescription());
        ledger.setMemo(request.getMemo());
        Long id = ledgerService.registLedger(ledger);

        return new CreateLedgerResponse(id);
    }
    @Data
    static class CreateLedgerRequest {
        @Email
        private LocalDateTime date;
        private BigDecimal amount;
        private String description;
        private String memo;
    }
    @Data
    static class CreateLedgerResponse {
        private Long id;
        public CreateLedgerResponse(Long id) {
            this.id = id;
        }
    }
}
