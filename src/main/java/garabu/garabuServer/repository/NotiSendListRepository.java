package garabu.garabuServer.repository;


import garabu.garabuServer.domain.NotiSendList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotiSendListRepository extends JpaRepository<NotiSendList, Long> {
    List<NotiSendList> findByAppIdAndNoticeNo(String appId, Long noticeNo);

}
