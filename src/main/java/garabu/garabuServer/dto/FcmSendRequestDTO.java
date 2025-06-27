package garabu.garabuServer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 푸시 발송 요청 입력 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "푸시 메시지 발송 요청 정보 DTO")
public class FcmSendRequestDTO {
    @Schema(description = "앱 ID", example = "PUSH_MGR")
    private String appId;

    @Schema(description = "푸시 제목", example = "긴급 점검 안내")
    private String noticeTitle;

    @Schema(description = "푸시 본문 내용", example = "내일 오전 3시에 시스템 점검이 예정되어 있습니다.")
    private String noticeBody;

    @Schema(description = "푸시 이미지 URL", example = "https://cdn.example.com/image.jpg")
    private String noticeImg;

    @Schema(description = "푸시 클릭 시 이동할 URL", example = "https://example.com/notice")
    private String noticeUrl;

    @Schema(description = "푸시 클릭 시 실행할 앱 액션", example = "open_notice_detail")
    private String noticeAction;

    @Schema(description = "발송자 ID", example = "admin01")
    private String userId;

    @Schema(description = "발송자 이름", example = "홍길동")
    private String userNm;

    @Schema(description = "발송자 연락처", example = "010-1234-5678")
    private String userMobile;

    @Schema(description = "푸시 사용 여부 (Y/N)", example = "Y")
    private String pushUse;

    @Schema(description = "SMS 사용 여부 (Y/N)", example = "N")
    private String smsUse;

    @Schema(description = "웹 푸시 사용 여부 (Y/N)", example = "Y")
    private String webUse;

    @Schema(description = "예약 발송 일시 (없을 경우 null)", example = "2025-05-20T09:00:00")
    private LocalDateTime reservationDt;

    @Schema(description = "사용자 이름 포함 여부 (Y/N)", example = "N")
    private String userNmAt;

    @Schema(description = "발송 대상 사용자 ID 목록 (콤마 구분)", example = "user1,user2,user3")
    private String sendUserList;
}
