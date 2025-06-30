package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 푸시 발송 마스터 테이블
 */
@Entity
@Table(name = "NOTI_SEND_M")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiSend {

    /**
     * 푸시 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTICE_NO")
    private Long noticeNo;

    /**
     * 앱 ID
     */
    @Column(name = "APP_ID")
    private String appId;

    // 연관관계 매핑 추가 (LAZY)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_ID", referencedColumnName = "APP_ID", insertable = false, updatable = false)
    private NotiApp app;

    /**
     * 푸시 제목
     */
    @Column(name = "NOTICE_TITLE")
    private String noticeTitle;

    /**
     * 푸시 내용
     */
    @Column(name = "NOTICE_BODY", columnDefinition = "TEXT")
    private String noticeBody;

    /**
     * 푸시 이미지 URL
     */
    @Column(name = "NOTICE_IMG")
    private String noticeImg;

    /**
     * 클릭 시 이동할 URL
     */
    @Column(name = "NOTICE_URL")
    private String noticeUrl;

    /**
     * 클릭 시 실행할 앱 액션
     */
    @Column(name = "NOTICE_ACTION")
    private String noticeAction;

    /**
     * 발송자 ID
     */
    @Column(name = "USER_ID")
    private String userId;

    /**
     * 발송자 이름
     */
    @Column(name = "USER_NM")
    private String userNm;

    /**
     * 발송자 연락처
     */
    @Column(name = "USER_MOBILE")
    private String userMobile;

    /**
     * 발송 일시
     */
    @Column(name = "NOTICE_DT")
    private LocalDateTime noticeDt;

    /**
     * 푸시 사용 여부
     */
    @Column(name = "PUSH_USE")
    private String pushUse;

    /**
     * 푸시 발송 대상 수
     */
    @Column(name = "PUSH_CNT")
    private Integer pushCnt;

    /**
     * 푸시 성공 수
     */
    @Column(name = "PUSH_SUCCESS_CNT")
    private Integer pushSuccessCnt;

    /**
     * 푸시 실패 수
     */
    @Column(name = "PUSH_FAIL_CNT")
    private Integer pushFailCnt;

    /**
     * SMS 사용 여부
     */
    @Column(name = "SMS_USE")
    private String smsUse;

    /**
     * SMS 발송 수
     */
    @Column(name = "SMS_CNT")
    private Integer smsCnt;

    /**
     * SMS 성공 수
     */
    @Column(name = "SMS_SUCCESS_CNT")
    private Integer smsSuccessCnt;

    /**
     * SMS 실패 수
     */
    @Column(name = "SMS_FAIL_CNT")
    private Integer smsFailCnt;

    /**
     * 웹 푸시 사용 여부
     */
    @Column(name = "WEB_USE")
    private String webUse;

    /**
     * 총 발송 대상 수
     */
    @Column(name = "TOTAL_CNT")
    private Integer totalCnt;

    /**
     * 예약 발송일시
     */
    @Column(name = "RESERVATION_DT")
    private LocalDateTime reservationDt;

    /**
     * 발송 상태 (대기/예약/완료 등)
     */
    @Column(name = "PUSH_STATE")
    private String pushState;

    /**
     * 사용자 이름 포함 여부
     */
    @Column(name = "USER_NM_AT")
    private String userNmAt;
}
