package garabu.garabuServer.dto.currency;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExchangeRateResponse {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private LocalDate rateDate;
    private String source;
} 