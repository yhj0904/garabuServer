package garabu.garabuServer.api;

import garabu.garabuServer.dto.MobileOAuthRequest;
import garabu.garabuServer.dto.MobileOAuthResponse;
import garabu.garabuServer.service.MobileOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 모바일 OAuth 인증 API 컨트롤러
 * 
 * <p>모바일 앱에서 OAuth 토큰을 받아 JWT 토큰을 발급하는 API를 제공합니다.
 * 기존 웹 OAuth 시스템과 독립적으로 동작하며, 동일한 Member 엔티티를 사용합니다.</p>
 * 
 * <p><strong>지원하는 OAuth 제공자:</strong>
 * <ul>
 *   <li>Google: Android, iOS 앱에서 네이티브 OAuth 토큰 사용</li>
 *   <li>Apple: iOS 앱에서 Sign in with Apple 토큰 사용</li>
 *   <li>Naver: 모바일 앱에서 네이버 OAuth 토큰 사용</li>
 *   <li>Kakao: 모바일 앱에서 카카오 OAuth 토큰 사용</li>
 * </ul></p>
 * 
 * @author Garabu Team
 * @version 2.0
 */
@RestController
@RequestMapping("/api/v2/mobile-oauth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mobile OAuth", description = "모바일 OAuth 인증 API - 소셜 로그인")
public class MobileOAuthController {
    
    private final MobileOAuthService mobileOAuthService;

    /**
     * 모바일 OAuth 로그인
     * 
     * <p>모바일 앱에서 받은 OAuth 토큰을 서버에서 검증하고 JWT 토큰을 발급합니다.</p>
     * 
     * <p><strong>처리 과정:</strong>
     * <ol>
     *   <li>클라이언트가 제공한 OAuth 토큰을 각 제공자 API로 검증</li>
     *   <li>사용자 정보 추출 (이메일, 이름, 프로필 이미지 등)</li>
     *   <li>기존 회원이면 로그인, 신규 회원이면 자동 가입</li>
     *   <li>JWT 액세스 토큰 + 리프레시 토큰 발급</li>
     * </ol></p>
     * 
     * @param request OAuth 제공자별 토큰 정보
     * @return JWT 토큰과 사용자 정보
     */
    @PostMapping("/login")
    @Operation(
            summary = "모바일 OAuth 로그인",
            description = """
                    모바일 앱에서 받은 OAuth 토큰으로 JWT 토큰을 발급받습니다.
                    
                    **지원하는 제공자:**
                    - `google`: Google OAuth 토큰 (Android/iOS)
                    - `apple`: Apple Sign-in 토큰 (iOS)
                    - `naver`: 네이버 OAuth 토큰
                    - `kakao`: 카카오 OAuth 토큰
                    
                    **응답 토큰:**
                    - `accessToken`: API 인증용 (10분 유효)
                    - `refreshToken`: 토큰 갱신용 (60일 유효)
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "OAuth 로그인 요청 정보",
            required = true,
            content = @Content(
                    schema = @Schema(implementation = MobileOAuthRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "Google OAuth",
                                    summary = "구글 OAuth 토큰 로그인",
                                    value = """
                                            {
                                              "provider": "google",
                                              "token": "ya29.a0AfH6SMBq...",
                                              "idToken": "eyJhbGciOiJSUzI1NiIs..."
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Apple OAuth",
                                    summary = "애플 OAuth 토큰 로그인",
                                    value = """
                                            {
                                              "provider": "apple",
                                              "token": "c7a4b2e8f3d9...",
                                              "idToken": "eyJhbGciOiJSUzI1NiIs..."
                                            }
                                            """
                            )
                    }
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = MobileOAuthResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
                                              "tokenType": "Bearer",
                                              "expiresIn": 600,
                                              "user": {
                                                "id": 1,
                                                "username": "홍길동",
                                                "email": "user@example.com",
                                                "profileImageUrl": "https://example.com/profile.jpg",
                                                "provider": "google"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 - 유효하지 않은 토큰 또는 제공자"),
            @ApiResponse(responseCode = "401", description = "인증 실패 - OAuth 토큰 검증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
     * <p>현재 Garabu 서비스에서 지원하는 OAuth 제공자 목록을 반환합니다.
     * 모바일 앱에서 로그인 UI를 구성할 때 이 정보를 사용할 수 있습니다.</p>
     * 
     * @return 지원하는 OAuth 제공자 목록
     */
    @GetMapping("/providers")
    @Operation(
            summary = "지원하는 OAuth 제공자 목록",
            description = """
                    현재 지원하는 OAuth 제공자 목록을 반환합니다.
                    
                    **지원하는 제공자:**
                    - `google`: Google OAuth 2.0
                    - `apple`: Apple Sign in with Apple
                    - `naver`: 네이버 OAuth 2.0
                    - `kakao`: 카카오 OAuth 2.0
                    
                    모바일 앱에서 로그인 버튼을 동적으로 생성할 때 사용하세요.
                    """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "제공자 목록 조회 성공",
                    content = @Content(
                            schema = @Schema(
                                    type = "array",
                                    description = "지원하는 OAuth 제공자 목록",
                                    example = "[\"google\", \"apple\", \"naver\", \"kakao\"]"
                            ),
                            examples = @ExampleObject(
                                    name = "제공자 목록",
                                    value = "[\"google\", \"apple\", \"naver\", \"kakao\"]"
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<String[]> getSupportedProviders() {
        String[] providers = {"google", "apple", "naver", "kakao"};
        return ResponseEntity.ok(providers);
    }
}