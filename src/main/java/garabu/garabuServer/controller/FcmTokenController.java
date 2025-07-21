package garabu.garabuServer.controller;


import garabu.garabuServer.dto.FcmTokenDeleteDTO;
import garabu.garabuServer.dto.FcmTokenRegisterDTO;
import garabu.garabuServer.service.FcmTokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/token")
@RequiredArgsConstructor
public class FcmTokenController {

    private final FcmTokenService fcmTokenService;

    @PostMapping("/register")
    @Operation(summary = "FCM 토큰 등록/업데이트")
    public ResponseEntity<Void> register(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FcmTokenRegisterDTO dto) {
        
        // 사용자 ID 설정
        dto.setUserId(userDetails.getUsername());
        dto.setAppId("garabu-app");
        
        fcmTokenService.registerOrUpdate(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "FCM 토큰 삭제")
    public ResponseEntity<Void> deleteToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FcmTokenDeleteDTO dto){
        
        // 사용자 ID 설정
        dto.setUserId(userDetails.getUsername());
        dto.setAppId("garabu-app");
        
        fcmTokenService.deleteByAppIdAndUserIdAndDeviceId(dto);
        return ResponseEntity.ok().build();
    }
}
