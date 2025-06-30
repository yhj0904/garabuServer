package garabu.garabuServer.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

/**
 * 푸시 서비스 대상 앱 마스터 테이블
 * 앱별 FCM 설정, 사용 여부 등을 관리
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiApp {

    /**
     * 앱 ID (Primary Key)
     */
    @Id
    @Column(name = "APP_ID")
    private String appId;

    /**
     * FCM V1 프로토콜 사용 여부 (Y/N)
     */
    @Column(name = "FCM_HTTP_V1_AT")
    private String fcmHttpV1At;

    /**
     * FCM API Key
     */
    @Column(name = "FCM_API_KEY", columnDefinition = "TEXT")
    private String fcmApiKey;

    /**
     * FCM V1 Configuration (서비스 계정 JSON 문자열)
     */
    @Column(name = "FCM_V1_CONFIG", columnDefinition = "TEXT")
    private String fcmV1Config;

    /**
     * 푸시 사용 여부 (Y/N)
     */
    @Column(name = "PUSH_USE_AT")
    private String pushUseAt;

    /**
     * SMS 사용 여부 (Y/N)
     */
    @Column(name = "SMS_USE_AT")
    private String smsUseAt;

    /**
     * 웹 푸시 사용 여부 (Y/N)
     */
    @Column(name = "WEB_USE_AT")
    private String webUseAt;

    /**
     * 앱 사용 상태 (Y/N)
     */
    @Column(name = "USE_AT")
    private String useAt;
}

