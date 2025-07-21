package garabu.garabuServer.dto.currency;

import lombok.Data;

@Data
public class BookCurrencyResponse {
    private Long bookId;
    private String defaultCurrency;
    private Boolean useMultiCurrency;
} 