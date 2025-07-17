package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메시지 타입 열거형
 */
@Schema(description = "메시지 타입")
public enum MessageType {
    @Schema(description = "텍스트 메시지")
    TEXT,
    
    @Schema(description = "이미지 메시지")
    IMAGE,
    
    @Schema(description = "파일 메시지")
    FILE,
    
    @Schema(description = "가계부 공유 메시지")
    BOOK_SHARE,
    
    @Schema(description = "위치 메시지")
    LOCATION,
    
    @Schema(description = "시스템 메시지")
    SYSTEM
}