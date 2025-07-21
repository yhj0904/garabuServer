package garabu.garabuServer.api;

import garabu.garabuServer.service.ReceiptService;
import garabu.garabuServer.dto.receipt.ReceiptResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v2/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipt", description = "영수증 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class ReceiptApiController {
    
    private final ReceiptService receiptService;
    
    @GetMapping("/book/{bookId}")
    @Operation(summary = "가계부 영수증 목록 조회")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId) {
        List<ReceiptResponse> receipts = receiptService.getReceiptsByBook(userDetails.getUsername(), bookId);
        return ResponseEntity.ok(receipts);
    }
    
    @PostMapping(value = "/book/{bookId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "영수증 이미지 업로드")
    public ResponseEntity<ReceiptResponse> uploadReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file) {
        ReceiptResponse receipt = receiptService.uploadReceipt(userDetails.getUsername(), bookId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }
    
    @DeleteMapping("/{receiptId}")
    @Operation(summary = "영수증 삭제")
    public ResponseEntity<Void> deleteReceipt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long receiptId) {
        receiptService.deleteReceipt(userDetails.getUsername(), receiptId);
        return ResponseEntity.ok().build();
    }
} 