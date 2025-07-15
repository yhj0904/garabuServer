package garabu.garabuServer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 이체 생성 요청 DTO
 */
@Schema(description = "이체 생성 요청")
public class CreateTransferRequest {

    @NotNull(message = "날짜는 필수입니다")
    @PastOrPresent(message = "날짜는 오늘 이전이어야 합니다")
    @Schema(description = "이체 날짜", example = "2024-01-01")
    private LocalDate date;

    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 양수여야 합니다")
    @Schema(description = "이체 금액", example = "100000")
    private Long amount;

    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 200, message = "내용은 200자 이하여야 합니다")
    @Schema(description = "이체 내용", example = "생활비 이체")
    private String description;

    @Size(max = 500, message = "메모는 500자 이하여야 합니다")
    @Schema(description = "메모", example = "월말 정산")
    private String memo;

    @NotNull(message = "가계부 ID는 필수입니다")
    @Schema(description = "가계부 ID", example = "1")
    private Long bookId;

    @Schema(description = "출금 자산 ID (출금 또는 입금 자산 중 하나는 필수)", example = "1")
    private Long fromAssetId;

    @Schema(description = "입금 자산 ID (출금 또는 입금 자산 중 하나는 필수)", example = "2")
    private Long toAssetId;

    @Size(max = 50, message = "이체자는 50자 이하여야 합니다")
    @Schema(description = "이체자", example = "홍길동")
    private String transferer;

    // 기본 생성자
    public CreateTransferRequest() {}

    // 생성자
    public CreateTransferRequest(LocalDate date, Long amount, String description, Long bookId, 
                               Long fromAssetId, Long toAssetId) {
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.bookId = bookId;
        this.fromAssetId = fromAssetId;
        this.toAssetId = toAssetId;
    }

    // Getter and Setter methods
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getFromAssetId() {
        return fromAssetId;
    }

    public void setFromAssetId(Long fromAssetId) {
        this.fromAssetId = fromAssetId;
    }

    public Long getToAssetId() {
        return toAssetId;
    }

    public void setToAssetId(Long toAssetId) {
        this.toAssetId = toAssetId;
    }

    public String getTransferer() {
        return transferer;
    }

    public void setTransferer(String transferer) {
        this.transferer = transferer;
    }
}