package garabu.garabuServer.api;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.dto.LoginUserDTO;
import garabu.garabuServer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원(Member) 관리 REST 컨트롤러
 *
 * <p>회원 가입·조회 등 사용자 관련 기능을 제공합니다.
 * 목록 조회는 JWT Bearer 인증이 필요하며, 가입은 공개 엔드포인트입니다.</p>
 *
 * @author yhj
 * @version 2.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")             // 공통 prefix
@Tag(name = "Member", description = "회원 관리 API")
public class MemberApiController {

    private final MemberService   memberService;
    private final PasswordEncoder passwordEncoder;

    /* ───────────────────────── 회원 목록 조회 ───────────────────────── */
    /**
     * 시스템에 등록된 모든 회원 정보를 조회합니다.
     *
     * @return 회원 리스트(Result 래퍼)
     */
    @GetMapping("/members")
    @Operation(
            summary     = "회원 목록 조회",
            description = "등록된 모든 회원 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "회원 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = Result.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Result<List<MemberDto>>> listMembers() {

        List<MemberDto> dtoList = memberService.findMembers().stream()
                .map(m -> new MemberDto(m.getUsername(),
                        m.getEmail(),
                        "*****")) // 비밀번호는 노출 금지
                .collect(Collectors.toList());

        return ResponseEntity.ok(new Result<>(dtoList));
    }

    /* ───────────────────────── 회원 가입 ───────────────────────── */
    /**
     * 새 회원을 등록합니다.
     *
     * @param request 가입 요청 DTO
     * @return 생성된 회원 ID
     */
    @PostMapping("/join")
    @Operation(
            summary     = "회원 가입",
            description = "새로운 사용자를 시스템에 등록합니다."
    )
    @RequestBody(
            required = true,
            description = "회원 가입 요청 본문",
            content = @Content(
                    schema = @Schema(implementation = CreateMemberRequest.class),
                    examples = @ExampleObject(
                            name  = "가입 예시",
                            value = "{\n"
                                    + "  \"email\": \"user@example.com\",\n"
                                    + "  \"username\": \"홍길동\",\n"
                                    + "  \"password\": \"password123\"\n"
                                    + "}"
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description  = "회원 가입 성공",
                    content      = @Content(schema = @Schema(implementation = CreateMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 사용자명")
    })
    public ResponseEntity<CreateMemberResponse> registerMember(
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateMemberRequest request) {

        Member member = new Member();
        member.setUsername(request.getUsername());
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));

        Long id = memberService.join(member);
        return ResponseEntity
                .status(201)
                .body(new CreateMemberResponse(id));
    }

    @GetMapping("/user/me")
    @Operation(
        summary = "현재 로그인한 사용자 정보 조회",
        description = "JWT 토큰을 통해 인증된 현재 사용자의 정보를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", 
            description = "사용자 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = LoginUserDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<LoginUserDTO> getCurrentUser() {
        LoginUserDTO currentUser = memberService.getCurrentLoginUserDTO();
        return ResponseEntity.ok(currentUser);
    }




    /* ───────────────────────── DTO 정의 ───────────────────────── */
    /** 공통 결과 래퍼 */
    @Data
    @AllArgsConstructor
    @Schema(description = "공통 응답 래퍼")
    static class Result<T> {
        @Schema(description = "응답 데이터")
        private T data;
    }

    /** 회원 정보 DTO */
    @Data
    @AllArgsConstructor
    @Schema(description = "회원 정보 DTO")
    static class MemberDto {
        @Schema(description = "사용자명", example = "홍길동")
        private String username;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "비밀번호(암호화)", example = "*****")
        private String password;
    }

    /** 회원 가입 요청 DTO */
    @Data
    @Schema(description = "회원 가입 요청 DTO")
    static class CreateMemberRequest {

        @Schema(description = "사용자 이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @Email
        private String email;

        @Schema(description = "사용자명", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        private String username;

        @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        private String password;
    }

    /** 회원 가입 응답 DTO */
    @Data
    @Schema(description = "회원 가입 응답 DTO")
    static class CreateMemberResponse {

        @Schema(description = "생성된 회원 ID", example = "15")
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
