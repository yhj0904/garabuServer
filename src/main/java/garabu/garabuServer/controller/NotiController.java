package garabu.garabuServer.controller;

import garabu.garabuServer.dto.FcmSendRequestDTO;
import garabu.garabuServer.service.FcmSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Push 발송", description = "푸시 발송 API")
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class NotiController {

    private final FcmSendService fcmSendService;

    @Operation(summary = "푸시 발송 요청", description = "푸시, 웹푸시, SMS 발송을 위한 요청을 처리합니다.")
    @PostMapping("/send")
    public ResponseEntity<Void> sendPush(@RequestBody FcmSendRequestDTO request) {
        fcmSendService.sendPush(request);
        return ResponseEntity.ok().build();
    }
}
