package garabu.garabuServer.repository;

import garabu.garabuServer.domain.NotiSendUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotiSendUserRepository extends JpaRepository<NotiSendUser, Long> {
    /**
     * APP_ID와 NOTICE_NO 기준으로 푸시 발송 대상자 목록 조회
     */
    List<NotiSendUser> findByAppIdAndNoticeNo(String appId, Long noticeNo);
}
