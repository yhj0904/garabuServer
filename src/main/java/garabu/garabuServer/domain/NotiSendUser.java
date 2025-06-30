package garabu.garabuServer.domain;


import jakarta.persistence.*;
import lombok.*;

/**
 * 푸시 개별 수신자 이력 테이블
 * 사용자별 푸시 발송 결과(성공 여부, 실패 메시지 등)를 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiSendUser {

    /**
     * 발송 시퀀스 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEND_SEQ")
    private Long sendSeq;

    /**
     * 앱 ID
     */
    @Column(name = "APP_ID")
    private String appId;

    /**
     * 공지사항(푸시) 일련번호
     */
    @Column(name = "NOTICE_NO")
    private Long noticeNo;

    /**
     * 수신자 사용자 ID
     */
    @Column(name = "USER_ID")
    private String userId;

    /**
     * 푸시 성공 여부 (Y/N)
     */
    @Column(name = "SUCCESS_YN")
    private String successYn;

    /**
     * 실패 시 오류 메시지
     */
    @Column(name = "FAIL_MSG")
    private String failMsg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NOTICE_NO", referencedColumnName = "NOTICE_NO", insertable = false, updatable = false)
    })
    private NotiSend pushSend;
}
