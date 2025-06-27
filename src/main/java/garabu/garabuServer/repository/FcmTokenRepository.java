package garabu.garabuServer.repository;

import garabu.garabuServer.domain.FcmUserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmUserToken, Long> {
    Optional<FcmUserToken> findTopByAppIdAndUserIdAndDeviceIdOrderByTokenIdDesc (String appId, String userId, String deviceId);
    Optional<FcmUserToken> findTopByAppIdAndUserIdAndUseAtOrderByTokenIdDesc(String appId, String userId, String useAt);
    Optional<FcmUserToken> findByAppIdAndUserIdAndDeviceId(String appId, String userId, String deviceId);

}
