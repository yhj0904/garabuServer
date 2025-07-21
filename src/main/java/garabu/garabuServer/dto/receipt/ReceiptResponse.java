package garabu.garabuServer.dto.receipt;

import garabu.garabuServer.domain.Receipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private Long id;
    private Long bookId;
    private Long uploadedById;
    private String uploadedByName;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String storeName;
    private LocalDate purchaseDate;
    private Integer totalAmount;
    private String status;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ReceiptResponse fromEntity(Receipt receipt) {
        return ReceiptResponse.builder()
                .id(receipt.getId())
                .bookId(receipt.getBook().getId())
                .uploadedById(receipt.getUploadedBy().getId())
                .uploadedByName(receipt.getUploadedBy().getName())
                .fileName(receipt.getFileName())
                .filePath(receipt.getFilePath())
                .fileType(receipt.getFileType())
                .fileSize(receipt.getFileSize())
                .storeName(receipt.getStoreName())
                .purchaseDate(receipt.getPurchaseDate())
                .totalAmount(receipt.getTotalAmount())
                .status(receipt.getStatus() != null ? receipt.getStatus().name() : null)
                .memo(receipt.getMemo())
                .createdAt(receipt.getCreatedAt())
                .updatedAt(receipt.getUpdatedAt())
                .build();
    }
} 