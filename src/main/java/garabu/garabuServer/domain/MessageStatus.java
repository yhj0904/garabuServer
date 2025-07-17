package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메시지 상태 열거형
 */
@Schema(description = "메시지 상태")
public enum MessageStatus {
    @Schema(description = "메시지 전송됨")
    SENT,
    
    @Schema(description = "메시지 전달됨")
    DELIVERED,
    
    @Schema(description = "메시지 읽음")
    READ,
    
    @Schema(description = "메시지 전송 실패")
    FAILED
}