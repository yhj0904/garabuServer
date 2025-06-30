package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 웹 푸시 발송 이력 테이블
 * 사용자별 발송 결과, 토큰, 실패 메시지 등을 저장
 */
@Entity
@Table(name = "T_PUSH_WEB_SEND_LIST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiWebSendList {

    /**
     * 웹 푸시 발송 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WEB_SEND_ID")
    private Long webSendId;

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
     * 웹 푸시 토큰
     */
    @Column(name = "WEB_TOKEN")
    private String webToken;

    /**
     * 발송 일시
     */
    @Column(name = "SEND_DT")
    private String sendDt;

    /**
     * 발송 성공 여부 (Y/N)
     */
    @Column(name = "SUCCESS_YN")
    private String successYn;

    /**
     * 실패 메시지
     */
    @Column(name = "FAIL_MSG")
    private String failMsg;
}
