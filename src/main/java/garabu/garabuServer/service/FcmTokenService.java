package garabu.garabuServer.service;

import garabu.garabuServer.dto.FcmTokenDeleteDTO;
import garabu.garabuServer.dto.FcmTokenRegisterDTO;

public interface FcmTokenService {
    void registerOrUpdate(FcmTokenRegisterDTO dto);
    void deleteByAppIdAndUserIdAndDeviceId(FcmTokenDeleteDTO dto);
    
    // 추가 메서드 - NotificationApiController에서 사용
    void registerFcmToken(FcmTokenRegisterDTO dto);
    void deleteFcmToken(String userId, String deviceId);
}