package garabu.garabuServer.api;

import garabu.garabuServer.dto.currency.*;
import garabu.garabuServer.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v2/currencies")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "통화 및 환율 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class CurrencyApiController {
    
    private final CurrencyService currencyService;
    
    @GetMapping
    @Operation(summary = "통화 목록 조회", description = "지원하는 모든 통화 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        List<CurrencyResponse> currencies = currencyService.getAllCurrencies();
        return ResponseEntity.ok(currencies);
    }
    
    @GetMapping("/rates/current")
    @Operation(summary = "현재 환율 조회", description = "두 통화 간의 현재 환율을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "환율 정보를 찾을 수 없음")
    })
    public ResponseEntity<ExchangeRateResponse> getCurrentRate(
            @Parameter(description = "원본 통화 코드", example = "USD") @RequestParam String from,
            @Parameter(description = "대상 통화 코드", example = "KRW") @RequestParam String to) {
        
        ExchangeRateResponse response = currencyService.getCurrentRate(from, to);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/rates/history")
    @Operation(summary = "환율 이력 조회", description = "지정된 기간의 환율 이력을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<ExchangeRateResponse>> getHistoricalRates(
            @Parameter(description = "원본 통화 코드") @RequestParam String from,
            @Parameter(description = "대상 통화 코드") @RequestParam String to,
            @Parameter(description = "시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ExchangeRateResponse> rates = currencyService.getHistoricalRates(from, to, startDate, endDate);
        return ResponseEntity.ok(rates);
    }
    
    @PostMapping("/convert")
    @Operation(summary = "통화 변환", description = "금액을 다른 통화로 변환합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변환 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "환율 정보를 찾을 수 없음")
    })
    public ResponseEntity<CurrencyConversionResponse> convertCurrency(
            @Valid @RequestBody CurrencyConversionRequest request) {
        
        CurrencyConversionResponse response = currencyService.convert(request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/books/{bookId}/currency")
    @Operation(summary = "가계부 기본 통화 설정", description = "가계부의 기본 통화와 다중 통화 사용 여부를 설정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<BookCurrencyResponse> updateBookCurrency(
            @Parameter(description = "가계부 ID") @PathVariable Long bookId,
            @Valid @RequestBody UpdateBookCurrencyRequest request) {
        
        BookCurrencyResponse response = currencyService.updateBookCurrency(bookId, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/rates")
    @Operation(summary = "환율 수동 업데이트", description = "환율을 수동으로 업데이트합니다. (관리자 기능)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<Void> updateExchangeRate(
            @Valid @RequestBody UpdateExchangeRateRequest request) {
        
        currencyService.updateExchangeRate(request);
        return ResponseEntity.noContent().build();
    }
} 