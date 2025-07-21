package garabu.garabuServer.dto.currency;

import lombok.Data;

@Data
public class CurrencyResponse {
    private Long id;
    private String currencyCode;
    private String currencyName;
    private String currencyNameKr;
    private String symbol;
    private Integer decimalPlaces;
} 