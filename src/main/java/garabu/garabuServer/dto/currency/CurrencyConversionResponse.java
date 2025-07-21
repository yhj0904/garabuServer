package garabu.garabuServer.dto.currency;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CurrencyConversionResponse {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
    private BigDecimal exchangeRate;
    private LocalDate rateDate;
} 