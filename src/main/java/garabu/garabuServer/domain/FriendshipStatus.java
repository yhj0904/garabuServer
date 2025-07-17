package garabu.garabuServer.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 친구 관계 상태 열거형
 */
@Schema(description = "친구 관계 상태")
public enum FriendshipStatus {
    @Schema(description = "친구 요청 대기 중")
    PENDING,
    
    @Schema(description = "친구 요청 수락됨")
    ACCEPTED,
    
    @Schema(description = "친구 요청 거절됨")
    REJECTED,
    
    @Schema(description = "친구 관계 차단됨")
    BLOCKED
}