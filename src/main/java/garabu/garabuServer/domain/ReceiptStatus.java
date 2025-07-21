package garabu.garabuServer.domain;

import lombok.Getter;

@Getter
public enum ReceiptStatus {
    UPLOADED("업로드됨", "영수증이 업로드됨"),
    OCR_PROCESSING("OCR 처리중", "OCR 처리가 진행 중"),
    OCR_COMPLETED("OCR 완료", "OCR 처리가 완료됨"),
    OCR_FAILED("OCR 실패", "OCR 처리가 실패함"),
    PROCESSED("처리완료", "거래 내역으로 변환 완료");
    
    private final String displayName;
    private final String description;
    
    ReceiptStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
} 