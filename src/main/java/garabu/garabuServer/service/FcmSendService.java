package garabu.garabuServer.service;

import garabu.garabuServer.dto.FcmSendRequestDTO;

public interface FcmSendService {

    /**
     * 푸시 발송 요청 처리
     * @param request 푸시 요청 DTO
     */
    void sendPush(FcmSendRequestDTO request);
}
