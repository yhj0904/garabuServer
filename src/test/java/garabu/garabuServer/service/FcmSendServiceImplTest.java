/*package garabu.garabuServer.service;

import garabu.garabuServer.domain.*;
import garabu.garabuServer.dto.FcmSendRequestDTO;
import garabu.garabuServer.repository.*;
import garabu.garabuServer.service.impl.FcmSendServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmSendServiceImplTest {

    @InjectMocks
    private FcmSendServiceImpl NotiSendService;

    @Mock
    private NotiSendRepository NotiSendRepository;
    @Mock private NotiAppRepository notiAppRepository;
    @Mock private NotiSendUserRepository NotiSendUserRepository;
    @Mock private NotiSendListRepository NotiSendListRepository;
    @Mock private NotiWebSendListRepository pushWebSendListRepository;
    @Mock private NotiSendStackRepository NotiSendStackRepository;
    @Mock private FcmTokenRepository NotiAppUserTokenRepository;
    @Mock private FcmService fcmService;



    @Test
    void testSendPush() {

        // Given
        FcmSendRequestDTO request = FcmSendRequestDTO.builder()
                .appId("MOBILE")
                .noticeTitle("Test Title")
                .noticeBody("Test Body")
                .noticeImg("http://example.com/image.png")
                .noticeUrl("http://example.com")
                .noticeAction("open_notice_detail")
                .userId("admin")
                .userNm("관리자")
                .userMobile("010-0000-0000")
                .sendUserList("user1,user2")
                .pushUse("Y")
                .smsUse("N")
                .webUse("Y")
                .userNmAt("N")
                .build();

        NotiApp app = NotiApp.builder()
                .pushUseAt("Y")
                .smsUseAt("N")
                .webUseAt("Y")
                .build();
        when(notiAppRepository.findById("MOBILE")).thenReturn(Optional.of(app));

        NotiSend savedNotiSend = NotiSend.builder()
                .noticeNo(1L)
                .build();
        when(NotiSendRepository.save(any())).thenReturn(savedNotiSend);

        List<NotiSendUser> users = List.of(
                NotiSendUser.builder()
                        .userId("nauri")
                        .noticeNo(1L)
                        .appId("MOBILE")
                        .successYn("N")
                        .build(),

                NotiSendUser.builder()
                        .userId("nauri1")
                        .noticeNo(1L)
                        .appId("MOBILE")
                        .successYn("N")
                        .build()
        );
        when(NotiSendUserRepository.findByAppIdAndNoticeNo("MOBILE",1L)).thenReturn(users);

        List<NotiSendList> pushList = users.stream()
                .map(u -> NotiSendList.builder()
                        .appId(u.getAppId()).noticeNo(u.getNoticeNo()).userId(u.getUserId())
                        .successYn("N").build())
                .toList();
        when(NotiSendListRepository.findByAppIdAndNoticeNo("MOBILE", 1L))
                .thenReturn(pushList);


        FcmUserToken token = FcmUserToken.builder().fcmToken("test_token").build();
        when(NotiAppUserTokenRepository.findTopByAppIdAndUserIdAndUseAtOrderByTokenIdDesc(any(),any(),any())).thenReturn(Optional.of(token));


        // When
        NotiSendService.sendPush(request);



        // Then
        verify(notiAppRepository).findById("MOBILE");
        verify(NotiSendRepository, times(3)).save(any());
        verify(NotiSendUserRepository).saveAll(anyList());
        verify(NotiSendListRepository, times(2)).saveAll(anyList());
        verify(pushWebSendListRepository).saveAll(anyList());
        verify(fcmService, times(2))
                .sendTo(eq("test_token"), anyString(), anyString());
        verify(NotiSendStackRepository).save(any());


    }

}
*/