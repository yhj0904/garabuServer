package garabu.garabuServer.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 푸시 발송 처리 로그(스택) 테이블
 * 푸시 메시지 발송 과정 중 단계별 실행 로그를 기록
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotiSendStack {

    /**
     * 스택 고유 ID (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STACK_ID")
    private Long stackId;

    /**
     * 앱 ID
     */
    @Column(name = "APP_ID")
    private String appId;

    /**
     * 공지사항(푸시) 번호
     */
    @Column(name = "NOTICE_NO")
    private Long noticeNo;

    /**
     * 로그 메시지
     */
    @Column(name = "STACK_MSG")
    private String stackMsg;

    /**
     * 처리 단계 구분 값
     */
    @Column(name = "STACK_STEP")
    private String stackStep;

    /**
     * 기록 시각
     */
    @Column(name = "LOG_DT")
    private LocalDateTime logDt;
}
