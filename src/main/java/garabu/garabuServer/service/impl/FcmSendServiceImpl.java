package garabu.garabuServer.service.impl;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.FcmSendRequestDTO;
import garabu.garabuServer.repository.*;
import garabu.garabuServer.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmSendServiceImpl {

    private final NotiAppRepository notiAppRepository;
    private final NotiSendRepository notiSendRepository;
    private final NotiSendUserRepository notiSendUserRepository;
    private final NotiSendListRepository notiSendListRepository;
    private final NotiWebSendListRepository notiWebSendListRepository;
    private final NotiSendStackRepository notiSendStackRepository;
    private final FcmService fcmService;
    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 푸시 발송 전체 처리
     */
    public void sendPush(FcmSendRequestDTO request) {
        NotiApp app = getAppSetting(request);                    // 1. 앱 설정 조회
        NotiSend pushSend = registerPushMaster(request);         // 2. 발송 마스터 등록
        registerRecipients(request, pushSend);                   // 3. 발송 대상자 등록
        createChannelLists(request, pushSend, app);              // 4. 채널별 발송 리스트 생성
        updateSendCounts(request, pushSend, app);                // 5. 발송 건수 및 상태 업데이트
        saveSendLog(request, pushSend);                          // 6. 처리 로그 저장
        sendFcmToTargets(request, pushSend);                     // 7. FCM 실제 발송 및 통계 반영
    }

    /**
     * Step 1 - APP 설정 조회 (푸시/SMS/웹 푸시 사용 여부 확인)
     */
    private NotiApp getAppSetting(FcmSendRequestDTO request) {
        NotiApp app = notiAppRepository.findById(request.getAppId())
                .orElseThrow(() -> new IllegalArgumentException("해당 앱이 존재하지 않습니다: " + request.getAppId()));
        log.info("앱 설정 조회 완료 - PUSH: {}, SMS: {}, WEB: {}",
                app.getPushUseAt(), app.getSmsUseAt(), app.getWebUseAt());
        return app;
    }

    /**
     * Step 2 - 푸시 발송 마스터 등록
     */
    private NotiSend registerPushMaster(FcmSendRequestDTO request) {
        NotiSend pushSend = NotiSend.builder()
                .appId(request.getAppId())
                .noticeTitle(request.getNoticeTitle())
                .noticeBody(request.getNoticeBody())
                .noticeImg(request.getNoticeImg())
                .noticeUrl(request.getNoticeUrl())
                .noticeAction(request.getNoticeAction())
                .userId(request.getUserId())
                .userNm(request.getUserNm())
                .userMobile(request.getUserMobile())
                .noticeDt(request.getReservationDt() != null ? request.getReservationDt() : LocalDateTime.now())
                .pushUse(request.getPushUse())
                .smsUse(request.getSmsUse())
                .webUse(request.getWebUse())
                .pushCnt(0)
                .pushSuccessCnt(0)
                .pushFailCnt(0)
                .smsCnt(0)
                .smsSuccessCnt(0)
                .smsFailCnt(0)
                .totalCnt(0)
                .pushState("WAIT")
                .userNmAt(request.getUserNmAt())
                .build();

        pushSend = notiSendRepository.save(pushSend);
        log.info("발송 마스터 저장 완료. NOTICE_NO = {}", pushSend.getNoticeNo());
        return pushSend;
    }

    /**
     * Step 3 - 발송 대상자 등록 (T_PUSH_SEND_USER)
     */
    private void registerRecipients(FcmSendRequestDTO request, NotiSend notiSend) {
        List<NotiSendUser> sendUsers = Arrays.stream(request.getSendUserList().split(","))
                .filter(u -> u != null && !u.trim().isEmpty())
                .map(userId -> NotiSendUser.builder()
                        .appId(request.getAppId())
                        .noticeNo(notiSend.getNoticeNo())
                        .userId(userId.trim())
                        .successYn("N")
                        .failMsg(null)
                        .build())
                .collect(Collectors.toList());

        notiSendUserRepository.saveAll(sendUsers);
        log.info("발송 대상자 {}명 저장 완료", sendUsers.size());
    }

    /**
     * Step 4 - 푸시 / 웹 푸시 발송 리스트 생성
     */
    private void createChannelLists(FcmSendRequestDTO request, NotiSend pushSend, NotiApp app) {
        List<NotiSendUser> users = notiSendUserRepository.findByAppIdAndNoticeNo(request.getAppId(), pushSend.getNoticeNo());

        if ("Y".equalsIgnoreCase(app.getPushUseAt())) {
            List<NotiSendList> pushList = users.stream().map(u ->
                    NotiSendList.builder()
                            .appId(u.getAppId())
                            .noticeNo(u.getNoticeNo())
                            .userId(u.getUserId())
                            .userNm(null)
                            .successYn("N")
                            .failMsg(null)
                            .sendDt(null)
                            .build()
            ).collect(Collectors.toList());
            notiSendListRepository.saveAll(pushList);
            log.info("PUSH 발송 대상자 {}명 저장 완료", pushList.size());
        }

        if ("Y".equalsIgnoreCase(app.getWebUseAt())) {
            List<NotiWebSendList> webList = users.stream().map(u ->
                    NotiWebSendList.builder()
                            .appId(u.getAppId())
                            .noticeNo(u.getNoticeNo())
                            .userId(u.getUserId())
                            .webToken(null)
                            .successYn("N")
                            .failMsg(null)
                            .sendDt(null)
                            .build()
            ).collect(Collectors.toList());
            notiWebSendListRepository.saveAll(webList);
            log.info("WEB PUSH 발송 대상자 {}명 저장 완료", webList.size());
        }

        if ("Y".equalsIgnoreCase(app.getSmsUseAt())) {
            log.info("SMS 대상자 처리 예정");
        }
    }

    /**
     * Step 5 - 발송 건수 집계 및 상태 업데이트
     */
    private void updateSendCounts(FcmSendRequestDTO request, NotiSend pushSend, NotiApp app) {
        List<NotiSendUser> users = notiSendUserRepository.findByAppIdAndNoticeNo(request.getAppId(), pushSend.getNoticeNo());
        int totalCnt = users.size();
        int pushCnt = "Y".equalsIgnoreCase(app.getPushUseAt()) ? totalCnt : 0;
        int smsCnt = "Y".equalsIgnoreCase(app.getSmsUseAt()) ? totalCnt : 0;
        String pushState = request.getReservationDt() != null ? "RESERVED" : "WAIT";

        pushSend.setTotalCnt(totalCnt);
        pushSend.setPushCnt(pushCnt);
        pushSend.setSmsCnt(smsCnt);
        pushSend.setPushState(pushState);

        notiSendRepository.save(pushSend);
        log.info("발송 마스터 상태 및 건수 업데이트 완료 - TOTAL: {}, PUSH: {}, SMS: {}, STATE: {}",
                totalCnt, pushCnt, smsCnt, pushState);
    }

    /**
     * Step 6 - 발송 처리 로그 저장 (T_PUSH_SEND_STACK)
     */
    private void saveSendLog(FcmSendRequestDTO request, NotiSend pushSend) {
        NotiSendStack logEntry = NotiSendStack.builder()
                .appId(request.getAppId())
                .noticeNo(pushSend.getNoticeNo())
                .stackMsg("발송 요청 수신 및 처리 완료")
                .stackStep("COMPLETE")
                .logDt(LocalDateTime.now())
                .build();

        notiSendStackRepository.save(logEntry);
        log.info("발송 처리 로그 저장 완료");
    }
    private void sendFcmToTargets(FcmSendRequestDTO request, NotiSend pushSend) {
        List<NotiSendList> targetList = notiSendListRepository.findByAppIdAndNoticeNo(
                request.getAppId(), pushSend.getNoticeNo());

        for (NotiSendList target : targetList) {
            try {
                Optional<FcmUserToken> tokenOpt = fcmTokenRepository
                        .findTopByAppIdAndUserIdAndUseAtOrderByTokenIdDesc(target.getAppId(), target.getUserId(), "Y");

                if (tokenOpt.isEmpty()) {
                    target.setSuccessYn("N");
                    target.setFailMsg("FCM 토큰 없음");
                    continue;
                }

                String token = tokenOpt.get().getFcmToken();

                fcmService.sendTo(token, request.getNoticeTitle(), request.getNoticeBody());

                target.setSuccessYn("Y");
                target.setSendDt(String.valueOf(LocalDateTime.now()));
            } catch (Exception e) {
                target.setSuccessYn("N");
                target.setFailMsg(e.getMessage());
            }
        }

        notiSendListRepository.saveAll(targetList);
        log.info("FCM 발송 대상 {}명 처리 완료", targetList.size());

        // Step 8: 성공/실패 통계 반영
        int successCnt = (int) targetList.stream().filter(t -> "Y".equalsIgnoreCase(t.getSuccessYn())).count();
        int failCnt = (int) targetList.stream().filter(t -> "N".equalsIgnoreCase(t.getSuccessYn())).count();

        pushSend.setPushSuccessCnt(successCnt);
        pushSend.setPushFailCnt(failCnt);
        notiSendRepository.save(pushSend);

        log.info("푸시 성공 수: {}, 실패 수: {} → PushSend 통계 반영 완료", successCnt, failCnt);
    }
}
