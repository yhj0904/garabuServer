package garabu.garabuServer.repository;

import garabu.garabuServer.domain.ExpoUserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpoUserTokenRepository extends JpaRepository<ExpoUserToken, Long> {
    
    Optional<ExpoUserToken> findByUserIdAndDeviceId(Long userId, String deviceId);
    
    Optional<ExpoUserToken> findByExpoToken(String expoToken);
    
    List<ExpoUserToken> findByUserId(Long userId);
    
    List<ExpoUserToken> findByUserIdAndActive(Long userId, boolean active);
    
    List<ExpoUserToken> findByUserIdInAndActive(List<Long> userIds, boolean active);
    
    List<ExpoUserToken> findByUpdatedAtBefore(LocalDateTime cutoffDate);
    
    @Modifying
    @Query("DELETE FROM ExpoUserToken e WHERE e.userId = :userId AND e.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
    
    @Modifying
    @Query("DELETE FROM ExpoUserToken e WHERE e.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}