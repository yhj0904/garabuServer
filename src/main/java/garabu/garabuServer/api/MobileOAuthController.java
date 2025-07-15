package garabu.garabuServer.api;

import garabu.garabuServer.dto.MobileOAuthRequest;
import garabu.garabuServer.dto.MobileOAuthResponse;
import garabu.garabuServer.service.MobileOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 모바일 OAuth 인증 API 컨트롤러
 * 
 * 모바일 앱에서 OAuth 토큰을 받아 JWT 토큰을 발급하는 API를 제공합니다.
 * 기존 웹 OAuth 시스템과 독립적으로 동작하며, 동일한 Member 엔티티를 사용합니다.
 */
@RestController
@RequestMapping("/api/v2/mobile-oauth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mobile OAuth", description = "모바일 OAuth 인증 API")
public class MobileOAuthController {
    
    private final MobileOAuthService mobileOAuthService;

    /**
     * 모바일 OAuth 로그인
     * 
     * @param request OAuth 제공자별 토큰 정보
     * @return JWT 토큰과 사용자 정보
     */
    @PostMapping("/login")
    @Operation(summary = "모바일 OAuth 로그인", description = "모바일 앱에서 받은 OAuth 토큰으로 JWT 토큰을 발급받습니다.")
    public ResponseEntity<MobileOAuthResponse> login(@Valid @RequestBody MobileOAuthRequest request) {
        log.info("모바일 OAuth 로그인 요청: provider={}", request.getProvider());
        
        try {
            MobileOAuthResponse response = mobileOAuthService.processOAuthLogin(request);
            log.info("모바일 OAuth 로그인 성공: user={}", response.getUser().getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("모바일 OAuth 로그인 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 지원하는 OAuth 제공자 목록 조회
     * 
     * @return 지원하는 OAuth 제공자 목록
     */
    @GetMapping("/providers")
    @Operation(summary = "지원하는 OAuth 제공자 목록", description = "현재 지원하는 OAuth 제공자 목록을 반환합니다.")
    public ResponseEntity<String[]> getSupportedProviders() {
        String[] providers = {"google", "apple", "naver", "kakao"};
        return ResponseEntity.ok(providers);
    }
}