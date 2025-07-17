package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 댓글 타입 열거형
 */
@Schema(description = "댓글 타입")
public enum CommentType {
    @Schema(description = "가계부 댓글")
    BOOK,
    
    @Schema(description = "가계부 내역 댓글")
    LEDGER
}