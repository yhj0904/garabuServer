package garabu.garabuServer.dto.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateExchangeRateRequest {
    
    @NotBlank(message = "원본 통화 코드는 필수입니다")
    private String fromCurrency;
    
    @NotBlank(message = "대상 통화 코드는 필수입니다")
    private String toCurrency;
    
    @NotNull(message = "환율은 필수입니다")
    @Positive(message = "환율은 양수여야 합니다")
    private BigDecimal rate;
    
    @NotNull(message = "환율 날짜는 필수입니다")
    private LocalDate rateDate;
} 