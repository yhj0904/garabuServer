package garabu.garabuServer.api;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원 관리 API 컨트롤러
 * 
 * 회원 가입, 회원 조회 등의 회원 관련 기능을 제공합니다.
 * 
 * @author Garabu Team
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관리 API")
public class MemberApiController {
    
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 모든 회원 정보를 조회합니다.
     * 
     * @return 회원 목록과 함께 Result 객체
     */
    @GetMapping("/api/v2/members")
    @Operation(
        summary = "회원 목록 조회",
        description = "시스템에 등록된 모든 회원의 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "회원 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = Result.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public Result memberV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getUsername(), m.getEmail(), m.getPassword() ))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    /**
     * 새로운 회원을 등록합니다.
     * 
     * @param request 회원 가입 요청 정보
     * @return 생성된 회원의 ID
     */
    @PostMapping("/join")
    @Operation(
        summary = "회원 가입",
        description = "새로운 회원을 시스템에 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "회원 가입 성공",
            content = @Content(schema = @Schema(implementation = CreateMemberResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 존재하는 이메일 또는 사용자명"
        )
    })
    public CreateMemberResponse saveMemberV2(
        @Parameter(description = "회원 가입 정보", required = true)
        @RequestBody @Valid CreateMemberRequest request) {
        
        Member member = new Member();
        member.setUsername(request.getUsername());
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
     * API 응답을 래핑하는 제네릭 클래스
     * 
     * @param <T> 응답 데이터의 타입
     */
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    /**
     * 회원 정보 DTO
     */
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String username;    // 사용자명
        private String email;       // 이메일
        private String password;    // 비밀번호 (암호화됨)
    }

    /**
     * 회원 가입 요청 DTO
     */
    @Data
    static class CreateMemberRequest {
        @Parameter(description = "사용자 이메일", example = "user@example.com")
        @Email
        private String email;
        
        @Parameter(description = "사용자명", example = "홍길동")
        private String username;
        
        @Parameter(description = "비밀번호", example = "password123")
        private String password;
    }
    
    /**
     * 회원 가입 응답 DTO
     */
    @Data
    static class CreateMemberResponse {
        @Parameter(description = "생성된 회원의 ID")
        private Long id;
        
        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}

