package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 푸시 발송 대상 목록 테이블
 * 개별 사용자에게 발송된 푸시 메시지 상태를 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiSendList {

    /**
     * 발송 대상 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEND_LIST_ID")
    private Long sendListId;

    /**
     * 앱 ID
     */
    @Column(name = "APP_ID")
    private String appId;

    /**
     * 푸시 공지 번호
     */
    @Column(name = "NOTICE_NO")
    private Long noticeNo;

    /**
     * 사용자 ID
     */
    @Column(name = "USER_ID")
    private String userId;

    /**
     * 사용자 이름
     */
    @Column(name = "USER_NM")
    private String userNm;

    /**
     * 발송 성공 여부 (Y/N)
     */
    @Column(name = "SUCCESS_YN")
    private String successYn;

    /**
     * 실패 시 메시지
     */
    @Column(name = "FAIL_MSG")
    private String failMsg;

    /**
     * 발송 일시
     */
    @Column(name = "SEND_DT")
    private String sendDt;

}
