package garabu.garabuServer.service;

import garabu.garabuServer.domain.ExpoUserToken;
import garabu.garabuServer.repository.ExpoUserTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExpoTokenService {
    
    private final ExpoUserTokenRepository expoUserTokenRepository;
    
    /**
     * Expo 푸시 토큰 등록/업데이트
     * 
     * @param userId 사용자 ID
     * @param deviceId 디바이스 ID
     * @param expoToken Expo 푸시 토큰
     */
    public void registerToken(Long userId, String deviceId, String expoToken) {
        // 기존 토큰 조회
        ExpoUserToken existingToken = expoUserTokenRepository
            .findByUserIdAndDeviceId(userId, deviceId)
            .orElse(null);
        
        if (existingToken != null) {
            // 기존 토큰 업데이트
            existingToken.setExpoToken(expoToken);
            existingToken.setUpdatedAt(LocalDateTime.now());
            existingToken.setActive(true);
            expoUserTokenRepository.save(existingToken);
            log.info("Expo 토큰 업데이트: userId={}, deviceId={}", userId, deviceId);
        } else {
            // 새 토큰 등록
            ExpoUserToken newToken = ExpoUserToken.builder()
                .userId(userId)
                .deviceId(deviceId)
                .expoToken(expoToken)
                .registeredAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build();
            expoUserTokenRepository.save(newToken);
            log.info("Expo 토큰 등록: userId={}, deviceId={}", userId, deviceId);
        }
    }
    
    /**
     * 특정 디바이스의 토큰 삭제
     * 
     * @param userId 사용자 ID
     * @param deviceId 디바이스 ID
     */
    public void deleteToken(Long userId, String deviceId) {
        expoUserTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
        log.info("Expo 토큰 삭제: userId={}, deviceId={}", userId, deviceId);
    }
    
    /**
     * 사용자의 모든 토큰 삭제
     * 
     * @param userId 사용자 ID
     */
    public void deleteAllTokensByUserId(Long userId) {
        expoUserTokenRepository.deleteByUserId(userId);
        log.info("사용자의 모든 Expo 토큰 삭제: userId={}", userId);
    }
    
    /**
     * 토큰 비활성화
     * 
     * @param expoToken Expo 토큰
     */
    public void deactivateToken(String expoToken) {
        expoUserTokenRepository.findByExpoToken(expoToken).ifPresent(token -> {
            token.setActive(false);
            token.setUpdatedAt(LocalDateTime.now());
            expoUserTokenRepository.save(token);
            log.info("Expo 토큰 비활성화: token={}", expoToken);
        });
    }
    
    /**
     * 사용자의 활성화된 토큰 조회
     * 
     * @param userId 사용자 ID
     * @return 활성화된 Expo 토큰 목록
     */
    public List<String> getActiveTokensByUserId(Long userId) {
        return expoUserTokenRepository.findByUserIdAndActive(userId, true)
            .stream()
            .map(ExpoUserToken::getExpoToken)
            .collect(Collectors.toList());
    }
    
    /**
     * 여러 사용자의 활성화된 토큰 조회
     * 
     * @param userIds 사용자 ID 목록
     * @return 활성화된 Expo 토큰 목록
     */
    public List<String> getActiveTokensByUserIds(List<Long> userIds) {
        return expoUserTokenRepository.findByUserIdInAndActive(userIds, true)
            .stream()
            .map(ExpoUserToken::getExpoToken)
            .collect(Collectors.toList());
    }
    
    /**
     * 오래된 토큰 정리 (30일 이상 업데이트되지 않은 토큰)
     */
    public void cleanupOldTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<ExpoUserToken> oldTokens = expoUserTokenRepository.findByUpdatedAtBefore(cutoffDate);
        
        if (!oldTokens.isEmpty()) {
            expoUserTokenRepository.deleteAll(oldTokens);
            log.info("오래된 Expo 토큰 {} 개 삭제", oldTokens.size());
        }
    }
}