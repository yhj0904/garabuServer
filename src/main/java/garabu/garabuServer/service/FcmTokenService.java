package garabu.garabuServer.service;

import garabu.garabuServer.dto.FcmTokenDeleteDTO;
import garabu.garabuServer.dto.FcmTokenRegisterDTO;

public interface FcmTokenService {
    void registerOrUpdate(FcmTokenRegisterDTO dto);
    void deleteByAppIdAndUserIdAndDeviceId(FcmTokenDeleteDTO dto);
}