package garabu.garabuServer.controller;


import garabu.garabuServer.dto.FcmTokenDeleteDTO;
import garabu.garabuServer.dto.FcmTokenRegisterDTO;
import garabu.garabuServer.service.FcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/register")
    @Operation(summary = "FCM 토큰 등록/업데이트")
    public ResponseEntity<Void> register(@RequestBody FcmTokenRegisterDTO dto) {
        fcmTokenService.registerOrUpdate(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "FCM 토큰 삭제")
    public ResponseEntity<Void> deleteToken(@RequestBody FcmTokenDeleteDTO dto){
        fcmTokenService.deleteByAppIdAndUserIdAndDeviceId(dto);
        return ResponseEntity.ok().build();
    }
}
