package garabu.garabuServer.repository;

import garabu.garabuServer.domain.NotiSend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotiSendRepository extends JpaRepository<NotiSend, Long> {
    Page<NotiSend> findByAppIdOrderByNoticeNoDesc(String appId, Pageable pageable);
    Optional<NotiSend> findByAppIdAndNoticeNo(String appId, Long noticeNo);
}
