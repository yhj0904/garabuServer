package garabu.garabuServer.service.impl;

import garabu.garabuServer.repository.FcmTokenRepository;
import garabu.garabuServer.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmSendServiceImpl {

    private final PushAppRepository pushAppRepository;
    private final PushSendRepository pushSendRepository;
    private final PushSendUserRepository pushSendUserRepository;
    private final PushSendListRepository pushSendListRepository;
    private final PushWebSendListRepository pushWebSendListRepository;
    private final PushSendStackRepository pushSendStackRepository;
    private final FcmService fcmService;
    private final FcmTokenRepository pushAppUserTokenRepository;
}
