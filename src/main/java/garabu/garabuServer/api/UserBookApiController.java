package garabu.garabuServer.api;

import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.event.BookEvent;
import garabu.garabuServer.event.BookEventPublisher;
import garabu.garabuServer.service.MemberService;
import garabu.garabuServer.service.UserBookService;
import garabu.garabuServer.service.PushNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 가계부(UserBook) 소유자 조회 REST 컨트롤러
 *
 * <p>bookId를 받아 해당 가계부를 소유한 회원(Owner) 목록을 반환합니다.
 * 모든 요청은 JWT Bearer 토큰 인증이 필요합니다.</p>
 *
 * @author yhj
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book")            // ⬅️ book 자원 하위로 위치
@Tag(name = "UserBook", description = "가계부 소유자 조회 API")
@SecurityRequirement(name = "bearerAuth")  // Swagger UI Authorize 버튼
public class UserBookApiController {

    private final UserBookService userBookService;
    private final BookEventPublisher bookEventPublisher;
    private final MemberService memberService;
    private final PushNotificationService pushNotificationService;

    /* ───────────────────────── 가계부 소유자 목록 조회 ───────────────────────── */
    /**
     * bookId에 해당하는 가계부를 소유한 사용자 목록을 조회합니다.
     *
     * @param bookId 조회할 가계부 ID
     * @return Owner 리스트
     */
    @GetMapping("/{bookId}/owners")
    @Operation(
            summary     = "가계부 소유자 목록 조회",
            description = "bookId에 해당하는 가계부를 소유(공유)하고 있는 모든 회원 정보를 반환합니다."
    )
    @Parameter(name = "bookId", description = "가계부 ID", required = true, example = "42")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description  = "소유자 목록 조회 성공",
                    content      = @Content(schema = @Schema(implementation = ListOwnerResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 가계부 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ListOwnerResponse> listOwnersByBookId(@PathVariable Long bookId) {

        /* 1) 서비스 호출 → UserBook 엔티티 목록 */
        List<UserBook> userBooks = userBookService.findOwnersByBookId(bookId);

        /* 2) DTO 변환 (회원 ID·이름·이메일·역할 포함) */
        List<OwnerDto> owners = userBooks.stream()
                .map(ub -> new OwnerDto(
                        ub.getMember().getId(),
                        ub.getMember().getUsername(),
                        ub.getMember().getEmail(),
                        ub.getBookRole()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ListOwnerResponse(owners));
    }

    /* ───────────────────────── 가계부 공유 초대 ───────────────────────── */
    /**
     * @deprecated 이메일 초대 방식은 아직 지원하지 않습니다. 초대 코드 방식을 사용하세요.
     * @see BookInviteApiController#createBookInviteCode
     * 
     * 가계부에 새로운 사용자를 초대합니다.
     *
     * @param bookId 가계부 ID
     * @param request 초대 요청 정보
     * @return 초대 결과
     */
    // @PostMapping("/{bookId}/invite")
    // @Operation(
    //         summary     = "가계부 공유 초대",
    //         description = "이메일로 사용자를 가계부에 초대합니다. 소유자만 초대할 수 있습니다."
    // )
    // @ApiResponses({
    //         @ApiResponse(responseCode = "200", description = "초대 성공"),
    //         @ApiResponse(responseCode = "404", description = "가계부 또는 사용자 없음"),
    //         @ApiResponse(responseCode = "403", description = "권한 없음 (소유자만 초대 가능)"),
    //         @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 참여 중인 사용자 등)"),
    //         @ApiResponse(responseCode = "401", description = "인증 실패")
    // })
    // public ResponseEntity<InviteResponse> inviteUser(
    //         @PathVariable Long bookId,
    //         @RequestBody @Valid InviteRequest request) {
    //     
    //     userBookService.inviteUser(bookId, request.getEmail(), request.getRole());
    //     return ResponseEntity.ok(new InviteResponse("사용자가 성공적으로 초대되었습니다."));
    // }

    /* ───────────────────────── 가계부 멤버 제거 ───────────────────────── */
    /**
     * 가계부에서 멤버를 제거합니다.
     *
     * @param bookId 가계부 ID
     * @param memberId 제거할 멤버 ID
     * @return 제거 결과
     */
    @DeleteMapping("/{bookId}/members/{memberId}")
    @Operation(
            summary     = "가계부 멤버 제거",
            description = "가계부에서 특정 멤버를 제거합니다. 소유자만 제거할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "멤버 제거 성공"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 멤버 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자만 제거 가능)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @Deprecated
    public ResponseEntity<RemoveMemberResponse> removeMember(
            @PathVariable Long bookId,
            @PathVariable Long memberId) {
        
        // 현재 사용자 정보 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        Member removedMember = memberService.findById(memberId);
        
        userBookService.removeMember(bookId, memberId);
        
        // Redis 이벤트 발행
        BookEvent event = BookEvent.memberRemoved(bookId, currentUser.getId(), memberId);
        bookEventPublisher.publishBookEvent(event);
        
        // 푸시 알림 발송 (멤버 제거)
        try {
            pushNotificationService.sendMemberRemovedNotification(bookId, removedMember, currentUser);
        } catch (Exception e) {
            // 푸시 알림 실패는 전체 작업에 영향을 주지 않음
        }
        
        return ResponseEntity.ok(new RemoveMemberResponse("멤버가 성공적으로 제거되었습니다."));
    }

    /* ───────────────────────── 가계부 멤버 권한 변경 ───────────────────────── */
    /**
     * 가계부 멤버의 권한을 변경합니다.
     *
     * @param bookId 가계부 ID
     * @param memberId 권한을 변경할 멤버 ID
     * @param request 권한 변경 요청 정보
     * @return 권한 변경 결과
     */
    @PutMapping("/{bookId}/members/{memberId}/role")
    @Operation(
            summary     = "가계부 멤버 권한 변경",
            description = "가계부 멤버의 권한을 변경합니다. 소유자만 변경할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "권한 변경 성공"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 멤버 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자만 변경 가능)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<ChangeRoleResponse> changeRole(
            @PathVariable Long bookId,
            @PathVariable Long memberId,
            @RequestBody @Valid ChangeRoleRequest request) {
        
        // 현재 사용자 정보 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        Member targetMember = memberService.findById(memberId);
        
        userBookService.changeRole(bookId, memberId, request.getRole());
        
        // Redis 이벤트 발행 - 권한 변경 이벤트 (memberUpdated 이벤트로 처리)
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("memberId", memberId);
        eventData.put("newRole", request.getRole());
        eventData.put("eventType", "ROLE_CHANGED");
        
        BookEvent event = BookEvent.builder()
                .bookId(bookId)
                .eventType("MEMBER_UPDATED")
                .data(eventData)
                .userId(currentUser.getId())
                .timestamp(System.currentTimeMillis())
                .build();
        bookEventPublisher.publishBookEvent(event);
        
        // 푸시 알림 발송 (권한 변경)
        try {
            pushNotificationService.sendRoleChangedNotification(bookId, targetMember, currentUser, request.getRole().name());
        } catch (Exception e) {
            // 푸시 알림 실패는 전체 작업에 영향을 주지 않음
        }
        
        return ResponseEntity.ok(new ChangeRoleResponse("권한이 성공적으로 변경되었습니다."));
    }

    /* ───────────────────────── 가계부 나가기 ───────────────────────── */
    /**
     * 현재 사용자가 가계부에서 나갑니다.
     *
     * @param bookId 가계부 ID
     * @return 나가기 결과
     */
    @PostMapping("/{bookId}/leave")
    @Operation(
            summary     = "가계부 나가기",
            description = "현재 사용자가 가계부에서 나갑니다. 소유자는 나갈 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "나가기 성공"),
            @ApiResponse(responseCode = "404", description = "가계부 또는 멤버 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유자는 나갈 수 없음)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<LeaveBookResponse> leaveBook(@PathVariable Long bookId) {
        
        // 현재 사용자 정보 조회
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        Long currentUserId = currentUser.getId();
        
        userBookService.leaveBook(bookId);
        
        // Redis 이벤트 발행 - 멤버 제거 이벤트
        BookEvent event = BookEvent.memberRemoved(bookId, currentUserId, currentUserId);
        bookEventPublisher.publishBookEvent(event);
        
        // 푸시 알림 발송 (멤버 탈퇴 - 다른 멤버들에게)
        try {
            pushNotificationService.sendMemberLeftNotification(bookId, currentUser);
        } catch (Exception e) {
            // 푸시 알림 실패는 전체 작업에 영향을 주지 않음
        }
        
        return ResponseEntity.ok(new LeaveBookResponse("가계부에서 성공적으로 나갔습니다."));
    }

    /* ───────────────────────── DTO 정의 ───────────────────────── */
    /** 가계부 소유자(회원) 요약 DTO */
    @Data
    @Schema(description = "가계부 소유자(회원) DTO")
    static class OwnerDto {
        @Schema(description = "회원 ID", example = "11")
        private Long memberId;

        @Schema(description = "회원 이름", example = "홍길동")
        private String username;

        @Schema(description = "회원 이메일", example = "user@example.com")
        private String email;

        @Schema(description = "가계부 내 역할", example = "OWNER")
        private BookRole role;

        public OwnerDto(Long memberId, String username, String email, BookRole role) {
            this.memberId = memberId;
            this.username = username;
            this.email = email;
            this.role = role;
        }
    }

    /** 가계부 소유자 목록 응답 DTO (래퍼) */
    @Data
    @Schema(description = "가계부 소유자 목록 응답 DTO")
    static class ListOwnerResponse {
        @Schema(description = "소유자 배열")
        private List<OwnerDto> owners;

        public ListOwnerResponse(List<OwnerDto> owners) {
            this.owners = owners;
        }
    }

    /* ───────────────────────── 공유 관련 DTO 정의 ───────────────────────── */
    
    /** 가계부 초대 요청 DTO */
    @Data
    @Schema(description = "가계부 초대 요청 DTO")
    static class InviteRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        @Schema(description = "초대할 사용자 이메일", example = "user@example.com")
        private String email;
        
        @NotNull(message = "역할은 필수입니다.")
        @Schema(description = "가계부 내 역할", example = "EDITOR")
        private BookRole role;
    }
    
    /** 가계부 초대 응답 DTO */
    @Data
    @Schema(description = "가계부 초대 응답 DTO")
    static class InviteResponse {
        @Schema(description = "응답 메시지", example = "사용자가 성공적으로 초대되었습니다.")
        private String message;
        
        public InviteResponse(String message) {
            this.message = message;
        }
    }
    
    /** 멤버 제거 응답 DTO */
    @Data
    @Schema(description = "멤버 제거 응답 DTO")
    static class RemoveMemberResponse {
        @Schema(description = "응답 메시지", example = "멤버가 성공적으로 제거되었습니다.")
        private String message;
        
        public RemoveMemberResponse(String message) {
            this.message = message;
        }
    }
    
    /** 권한 변경 요청 DTO */
    @Data
    @Schema(description = "권한 변경 요청 DTO")
    static class ChangeRoleRequest {
        @NotNull(message = "역할은 필수입니다.")
        @Schema(description = "변경할 역할", example = "VIEWER")
        private BookRole role;
    }
    
    /** 권한 변경 응답 DTO */
    @Data
    @Schema(description = "권한 변경 응답 DTO")
    static class ChangeRoleResponse {
        @Schema(description = "응답 메시지", example = "권한이 성공적으로 변경되었습니다.")
        private String message;
        
        public ChangeRoleResponse(String message) {
            this.message = message;
        }
    }
    
    /** 가계부 나가기 응답 DTO */
    @Data
    @Schema(description = "가계부 나가기 응답 DTO")
    static class LeaveBookResponse {
        @Schema(description = "응답 메시지", example = "가계부에서 성공적으로 나갔습니다.")
        private String message;
        
        public LeaveBookResponse(String message) {
            this.message = message;
        }
    }
}
