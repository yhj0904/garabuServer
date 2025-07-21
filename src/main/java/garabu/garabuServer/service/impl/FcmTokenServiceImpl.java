package garabu.garabuServer.service.impl;

import garabu.garabuServer.domain.FcmUserToken;
import garabu.garabuServer.dto.FcmTokenDeleteDTO;
import garabu.garabuServer.dto.FcmTokenRegisterDTO;
import garabu.garabuServer.repository.FcmTokenRepository;
import garabu.garabuServer.service.FcmTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenServiceImpl implements FcmTokenService {
    private final FcmTokenRepository tokenRepository;

    @Transactional
    @Override
    public void registerOrUpdate(FcmTokenRegisterDTO dto) {
        try {
            log.info("FCM 토큰 등록/업데이트 시작: userId={}, deviceId={}", dto.getUserId(), dto.getDeviceId());
            
            // 필수 필드 검증
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                throw new IllegalArgumentException("사용자 ID는 필수입니다.");
            }
            if (dto.getDeviceId() == null || dto.getDeviceId().trim().isEmpty()) {
                throw new IllegalArgumentException("디바이스 ID는 필수입니다.");
            }
            if (dto.getFcmToken() == null || dto.getFcmToken().trim().isEmpty()) {
                throw new IllegalArgumentException("FCM 토큰은 필수입니다.");
            }

            FcmUserToken token = tokenRepository
                    .findTopByAppIdAndUserIdAndDeviceIdOrderByTokenIdDesc(dto.getAppId(), dto.getUserId(), dto.getDeviceId())
                    .orElseGet(() -> FcmUserToken.builder()
                            .appId(dto.getAppId())
                            .userId(dto.getUserId())
                            .deviceId(dto.getDeviceId())
                            .regDt(LocalDateTime.now())
                            .build());

            token.setFcmToken(dto.getFcmToken());
            token.setUseAt("Y");
            token.setRegDt(LocalDateTime.now());

            tokenRepository.save(token);
            log.info("FCM 토큰 등록 또는 갱신 완료: userId={}, deviceId={}, token={}", 
                    dto.getUserId(), dto.getDeviceId(), dto.getFcmToken());
        } catch (Exception e) {
            log.error("FCM 토큰 등록/업데이트 실패: userId={}, deviceId={}, error={}", 
                    dto.getUserId(), dto.getDeviceId(), e.getMessage(), e);
            throw e;
        }
    }

    public void deleteByAppIdAndUserIdAndDeviceId(FcmTokenDeleteDTO dto) {
        Optional<FcmUserToken> tokenOpt = tokenRepository.findByAppIdAndUserIdAndDeviceId(
                dto.getAppId(), dto.getUserId(), dto.getDeviceId());

        if (tokenOpt.isPresent()) {
            FcmUserToken token = tokenOpt.get();
            token.setUseAt("N");
            token.setFcmToken(null);
            token.setRegDt(LocalDateTime.now());
            tokenRepository.save(token);
            log.info("FCM 토큰 삭제 처리 완료: {}, {}", dto.getUserId(), dto.getDeviceId());
        } else {
            log.warn("삭제할 토큰이 존재하지 않음: {}, {}", dto.getUserId(), dto.getDeviceId());
        }
    }

    @Override
    public void registerFcmToken(FcmTokenRegisterDTO dto) {
        registerOrUpdate(dto);
    }

    @Override
    public void deleteFcmToken(String userId, String deviceId) {
        FcmTokenDeleteDTO deleteDTO = FcmTokenDeleteDTO.builder()
                .appId("garabu-app")
                .userId(userId)
                .deviceId(deviceId)
                .build();
        deleteByAppIdAndUserIdAndDeviceId(deleteDTO);
    }

}
