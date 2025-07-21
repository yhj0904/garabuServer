package garabu.garabuServer.dto.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateBookCurrencyRequest {
    
    @NotBlank(message = "통화 코드는 필수입니다")
    @Pattern(regexp = "^[A-Z]{3}$", message = "통화 코드는 3자리 대문자여야 합니다")
    private String currencyCode;
    
    @NotNull(message = "다중 통화 사용 여부는 필수입니다")
    private Boolean useMultiCurrency;
} 