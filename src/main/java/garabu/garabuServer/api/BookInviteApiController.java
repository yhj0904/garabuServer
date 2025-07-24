package garabu.garabuServer.api;

import garabu.garabuServer.domain.BookRole;
import garabu.garabuServer.domain.UserBookGroup;
import garabu.garabuServer.domain.UserBookRequest;
import garabu.garabuServer.service.BookInviteService;
import garabu.garabuServer.service.InviteCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 가계부 초대 및 참가 요청 관리 API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book/invite")
@Tag(name = "BookInvite", description = "가계부 초대 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class BookInviteApiController {
    
    private final BookInviteService bookInviteService;
    private final InviteCodeService inviteCodeService;
    
    /**
     * 가계부 초대 코드 생성
     */
    @PostMapping("/{bookId}/code")
    @Operation(summary = "가계부 초대 코드 생성", description = "OWNER만 가계부 초대 코드를 생성할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초대 코드 생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<InviteCodeResponse> createBookInviteCode(
            @PathVariable Long bookId,
            @RequestBody @Valid CreateInviteCodeRequest request) {
        
        String code = bookInviteService.createBookInviteCode(bookId, request.getRole());
        long ttl = inviteCodeService.getCodeTTL(code, "BOOK");
        
        return ResponseEntity.ok(new InviteCodeResponse(code, ttl));
    }
    
    /**
     * 사용자 식별 코드 생성
     */
    @PostMapping("/user/code")
    @Operation(summary = "사용자 식별 코드 생성", description = "현재 사용자의 식별 코드를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "식별 코드 생성 성공")
    })
    public ResponseEntity<InviteCodeResponse> createUserIdCode() {
        String code = bookInviteService.createUserIdCode();
        long ttl = inviteCodeService.getCodeTTL(code, "USER");
        
        return ResponseEntity.ok(new InviteCodeResponse(code, ttl));
    }
    
    /**
     * 초대 코드로 가계부 참가 요청
     */
    @PostMapping("/join")
    @Operation(summary = "가계부 참가 요청", description = "초대 코드를 사용하여 가계부 참가를 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "참가 요청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 초대 코드")
    })
    public ResponseEntity<JoinRequestResponse> requestJoinBook(
            @RequestBody @Valid JoinBookRequest request) {
        
        UserBookRequest joinRequest = bookInviteService.requestJoinBook(request.getInviteCode());
        
        return ResponseEntity.ok(JoinRequestResponse.from(joinRequest));
    }
    
    /**
     * 참가 요청 수락
     */
    @PutMapping("/request/{requestId}/accept")
    @Operation(summary = "참가 요청 수락", description = "OWNER만 참가 요청을 수락할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수락 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음")
    })
    public ResponseEntity<Void> acceptJoinRequest(@PathVariable Long requestId) {
        bookInviteService.acceptJoinRequest(requestId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 참가 요청 거절
     */
    @PutMapping("/request/{requestId}/reject")
    @Operation(summary = "참가 요청 거절", description = "OWNER만 참가 요청을 거절할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음")
    })
    public ResponseEntity<Void> rejectJoinRequest(@PathVariable Long requestId) {
        bookInviteService.rejectJoinRequest(requestId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 가계부의 참가 요청 목록 조회
     */
    @GetMapping("/{bookId}/requests")
    @Operation(summary = "가계부 참가 요청 목록 조회", description = "OWNER만 조회할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<List<JoinRequestResponse>> getBookJoinRequests(@PathVariable Long bookId) {
        List<UserBookRequest> requests = bookInviteService.getBookJoinRequests(bookId);
        List<JoinRequestResponse> responses = requests.stream()
                .map(JoinRequestResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 내가 요청한 가계부 목록 조회
     */
    @GetMapping("/my-requests")
    @Operation(summary = "내 참가 요청 목록 조회", description = "현재 사용자가 요청한 가계부 참가 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<JoinRequestResponse>> getMyJoinRequests() {
        List<UserBookRequest> requests = bookInviteService.getMyJoinRequests();
        List<JoinRequestResponse> responses = requests.stream()
                .map(JoinRequestResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 그룹 생성
     */
    @PostMapping("/{bookId}/group")
    @Operation(summary = "가계부 그룹 생성", description = "OWNER만 그룹을 생성할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "그룹 생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<GroupResponse> createGroup(
            @PathVariable Long bookId,
            @RequestBody @Valid CreateGroupRequest request) {
        
        UserBookGroup group = bookInviteService.createGroup(
                bookId, request.getGroupName(), request.getDescription());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
    }
    
    /**
     * 그룹에 멤버 추가
     */
    @PostMapping("/group/{groupId}/member")
    @Operation(summary = "그룹에 멤버 추가", description = "OWNER만 그룹에 멤버를 추가할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "멤버 추가 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "그룹 또는 멤버를 찾을 수 없음")
    })
    public ResponseEntity<Void> addMemberToGroup(
            @PathVariable Long groupId,
            @RequestBody @Valid AddGroupMemberRequest request) {
        
        bookInviteService.addMemberToGroup(groupId, request.getUserBookId());
        return ResponseEntity.ok().build();
    }
    
    /**
     * 가계부의 그룹 목록 조회
     */
    @GetMapping("/{bookId}/groups")
    @Operation(summary = "가계부 그룹 목록 조회", description = "가계부의 그룹 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "404", description = "가계부를 찾을 수 없음")
    })
    public ResponseEntity<List<GroupResponse>> getBookGroups(@PathVariable Long bookId) {
        List<UserBookGroup> groups = bookInviteService.getBookGroups(bookId);
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    // DTO 클래스들
    
    @Data
    @Schema(description = "초대 코드 생성 요청")
    static class CreateInviteCodeRequest {
        @NotNull(message = "역할은 필수입니다")
        @Schema(description = "부여할 역할", example = "EDITOR")
        private BookRole role;
    }
    
    @Data
    @Schema(description = "초대 코드 응답")
    static class InviteCodeResponse {
        @Schema(description = "8자리 초대 코드", example = "12345678")
        private String code;
        
        @Schema(description = "남은 유효 시간(초)", example = "1800")
        private long ttlSeconds;
        
        public InviteCodeResponse(String code, long ttlSeconds) {
            this.code = code;
            this.ttlSeconds = ttlSeconds;
        }
    }
    
    @Data
    @Schema(description = "가계부 참가 요청")
    static class JoinBookRequest {
        @NotBlank(message = "초대 코드는 필수입니다")
        @Schema(description = "8자리 초대 코드", example = "12345678")
        private String inviteCode;
    }
    
    @Data
    @Schema(description = "참가 요청 응답")
    static class JoinRequestResponse {
        @Schema(description = "요청 ID")
        private Long requestId;
        
        @Schema(description = "가계부 ID")
        private Long bookId;
        
        @Schema(description = "가계부 제목")
        private String bookTitle;
        
        @Schema(description = "요청자 ID")
        private Long memberId;
        
        @Schema(description = "요청자 이름")
        private String memberName;
        
        @Schema(description = "요청자 이메일")
        private String memberEmail;
        
        @Schema(description = "요청 상태")
        private String status;
        
        @Schema(description = "요청한 역할")
        private String requestedRole;
        
        @Schema(description = "요청 일시")
        private LocalDateTime requestDate;
        
        @Schema(description = "응답 일시")
        private LocalDateTime responseDate;
        
        public static JoinRequestResponse from(UserBookRequest request) {
            JoinRequestResponse response = new JoinRequestResponse();
            response.requestId = request.getId();
            response.bookId = request.getBook().getId();
            response.bookTitle = request.getBook().getTitle();
            response.memberId = request.getMember().getId();
            response.memberName = request.getMember().getUsername();
            response.memberEmail = request.getMember().getEmail();
            response.status = request.getStatus().name();
            response.requestedRole = request.getRequestedRole().name();
            response.requestDate = request.getRequestDate();
            response.responseDate = request.getResponseDate();
            return response;
        }
    }
    
    @Data
    @Schema(description = "그룹 생성 요청")
    static class CreateGroupRequest {
        @NotBlank(message = "그룹명은 필수입니다")
        @Schema(description = "그룹명", example = "가족")
        private String groupName;
        
        @Schema(description = "그룹 설명", example = "가족 구성원들")
        private String description;
    }
    
    @Data
    @Schema(description = "그룹 멤버 추가 요청")
    static class AddGroupMemberRequest {
        @NotNull(message = "UserBook ID는 필수입니다")
        @Schema(description = "추가할 UserBook ID")
        private Long userBookId;
    }
    
    @Data
    @Schema(description = "그룹 응답")
    static class GroupResponse {
        @Schema(description = "그룹 ID")
        private Long groupId;
        
        @Schema(description = "그룹명")
        private String groupName;
        
        @Schema(description = "그룹 설명")
        private String description;
        
        @Schema(description = "생성일시")
        private LocalDateTime createdDate;
        
        @Schema(description = "멤버 수")
        private int memberCount;
        
        public static GroupResponse from(UserBookGroup group) {
            GroupResponse response = new GroupResponse();
            response.groupId = group.getId();
            response.groupName = group.getGroupName();
            response.description = group.getDescription();
            response.createdDate = group.getCreatedDate();
            response.memberCount = group.getGroupMembers() != null ? group.getGroupMembers().size() : 0;
            return response;
        }
    }
} 