package garabu.garabuServer.api;

import garabu.garabuServer.domain.Friendship;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.service.FriendshipService;
import garabu.garabuServer.service.MemberService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 친구 관계 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v2/friends")
@RequiredArgsConstructor
@Tag(name = "Friendship", description = "친구 관계 API")
@SecurityRequirement(name = "bearerAuth")
public class FriendshipApiController {
    
    private final FriendshipService friendshipService;
    private final MemberService memberService;
    private final InviteCodeService inviteCodeService;
    
    /**
     * 친구 요청 보내기
     */
    @PostMapping("/request")
    @Operation(summary = "친구 요청 보내기", description = "다른 사용자에게 친구 요청을 보냅니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "친구 요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "409", description = "이미 친구 요청 존재")
    })
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @Valid @RequestBody FriendRequestInputDto request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Friendship friendship = friendshipService.sendFriendRequest(
            currentUser.getId(), request.getAddresseeId(), request.getAlias());
        
        return ResponseEntity.ok(new FriendRequestResponse(
            "친구 요청이 성공적으로 전송되었습니다.",
            friendship.getId(),
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 요청 수락
     */
    @PostMapping("/accept/{friendshipId}")
    @Operation(summary = "친구 요청 수락", description = "받은 친구 요청을 수락합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "친구 요청 수락 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "친구 요청 없음")
    })
    public ResponseEntity<FriendRequestResponse> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @Valid @RequestBody FriendAcceptDto request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Friendship friendship = friendshipService.acceptFriendRequest(
            friendshipId, currentUser.getId(), request.getAlias());
        
        return ResponseEntity.ok(new FriendRequestResponse(
            "친구 요청이 성공적으로 수락되었습니다.",
            friendship.getId(),
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 요청 거절
     */
    @PostMapping("/reject/{friendshipId}")
    @Operation(summary = "친구 요청 거절", description = "받은 친구 요청을 거절합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "친구 요청 거절 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "친구 요청 없음")
    })
    public ResponseEntity<FriendRequestResponse> rejectFriendRequest(
            @PathVariable Long friendshipId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        friendshipService.rejectFriendRequest(friendshipId, currentUser.getId());
        
        return ResponseEntity.ok(new FriendRequestResponse(
            "친구 요청이 거절되었습니다.",
            friendshipId,
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 관계 삭제
     */
    @DeleteMapping("/{friendshipId}")
    @Operation(summary = "친구 관계 삭제", description = "친구 관계를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "친구 관계 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "친구 관계 없음")
    })
    public ResponseEntity<FriendRequestResponse> deleteFriendship(
            @PathVariable Long friendshipId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        friendshipService.deleteFriendship(friendshipId, currentUser.getId());
        
        return ResponseEntity.ok(new FriendRequestResponse(
            "친구 관계가 삭제되었습니다.",
            friendshipId,
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 별칭 설정
     */
    @PutMapping("/{friendshipId}/alias")
    @Operation(summary = "친구 별칭 설정", description = "친구의 별칭을 설정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "별칭 설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "친구 관계 없음")
    })
    public ResponseEntity<FriendRequestResponse> setFriendAlias(
            @PathVariable Long friendshipId,
            @Valid @RequestBody FriendAliasDto request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        friendshipService.setFriendAlias(friendshipId, currentUser.getId(), request.getAlias());
        
        return ResponseEntity.ok(new FriendRequestResponse(
            "친구 별칭이 설정되었습니다.",
            friendshipId,
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 목록 조회
     */
    @GetMapping
    @Operation(summary = "친구 목록 조회", description = "현재 사용자의 친구 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "친구 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<FriendListResponse> getFriends(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Page<Friendship> friendships = friendshipService.getFriends(currentUser.getId(), pageable);
        
        List<FriendDto> friends = friendships.getContent().stream()
            .map(friendship -> convertToFriendDto(friendship, currentUser))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new FriendListResponse(
            friends,
            friendships.getTotalElements(),
            friendships.getTotalPages(),
            friendships.getNumber(),
            friendships.getSize()
        ));
    }
    
    /**
     * 받은 친구 요청 목록 조회
     */
    @GetMapping("/requests/received")
    @Operation(summary = "받은 친구 요청 목록 조회", description = "현재 사용자가 받은 친구 요청 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "친구 요청 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<FriendRequestListResponse> getReceivedFriendRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Page<Friendship> requests = friendshipService.getReceivedFriendRequests(currentUser.getId(), pageable);
        
        List<FriendRequestDto> friendRequests = requests.getContent().stream()
            .map(this::convertToFriendRequestDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new FriendRequestListResponse(
            friendRequests,
            requests.getTotalElements(),
            requests.getTotalPages(),
            requests.getNumber(),
            requests.getSize()
        ));
    }
    
    /**
     * 보낸 친구 요청 목록 조회
     */
    @GetMapping("/requests/sent")
    @Operation(summary = "보낸 친구 요청 목록 조회", description = "현재 사용자가 보낸 친구 요청 목록을 조회합니다.")
    public ResponseEntity<FriendRequestListResponse> getSentFriendRequests(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Page<Friendship> requests = friendshipService.getSentFriendRequests(currentUser.getId(), pageable);
        
        List<FriendRequestDto> friendRequests = requests.getContent().stream()
            .map(this::convertToFriendRequestDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new FriendRequestListResponse(
            friendRequests,
            requests.getTotalElements(),
            requests.getTotalPages(),
            requests.getNumber(),
            requests.getSize()
        ));
    }
    
    /**
     * 친구 검색
     */
    @GetMapping("/search")
    @Operation(summary = "친구 검색", description = "사용자명으로 친구를 검색합니다.")
    public ResponseEntity<FriendListResponse> searchFriends(
            @Parameter(description = "검색할 사용자명") @RequestParam String username,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Page<Friendship> friendships = friendshipService.searchFriends(currentUser.getId(), username, pageable);
        
        List<FriendDto> friends = friendships.getContent().stream()
            .map(friendship -> convertToFriendDto(friendship, currentUser))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new FriendListResponse(
            friends,
            friendships.getTotalElements(),
            friendships.getTotalPages(),
            friendships.getNumber(),
            friendships.getSize()
        ));
    }
    
    /**
     * 친구 상태 정보 조회
     */
    @GetMapping("/status")
    @Operation(summary = "친구 상태 정보 조회", description = "친구 수, 대기 중인 요청 수 등을 조회합니다.")
    public ResponseEntity<FriendStatusDto> getFriendStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        long friendCount = friendshipService.getFriendCount(currentUser.getId());
        long pendingCount = friendshipService.getPendingRequestCount(currentUser.getId());
        
        return ResponseEntity.ok(new FriendStatusDto(friendCount, pendingCount));
    }
    
    /**
     * 친구 초대 코드 생성
     */
    @PostMapping("/invite-code")
    @Operation(summary = "친구 초대 코드 생성", description = "친구 초대를 위한 8자리 코드를 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "코드 생성 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<FriendInviteCodeResponse> createFriendInviteCode() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        String code = inviteCodeService.generateFriendInviteCode(currentUser.getId());
        long ttlSeconds = inviteCodeService.getCodeTTL(code, "FRIEND");
        
        return ResponseEntity.ok(new FriendInviteCodeResponse(code, ttlSeconds));
    }
    
    /**
     * 친구 초대 코드로 친구 요청 보내기
     */
    @PostMapping("/request-by-code")
    @Operation(summary = "초대 코드로 친구 요청", description = "친구 초대 코드를 사용하여 친구 요청을 보냅니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "친구 요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 코드"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "409", description = "이미 친구 요청 존재")
    })
    public ResponseEntity<FriendRequestResponse> sendFriendRequestByCode(
            @Valid @RequestBody FriendRequestByCodeDto request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        // 초대 코드로 사용자 ID 조회
        Long addresseeId = inviteCodeService.getUserIdByFriendInviteCode(request.getInviteCode());
        if (addresseeId == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 초대 코드입니다.");
        }
        
        // 자기 자신에게 친구 요청을 보내는 것 방지
        if (addresseeId.equals(currentUser.getId())) {
            throw new IllegalArgumentException("자기 자신에게는 친구 요청을 보낼 수 없습니다.");
        }
        
        Friendship friendship = friendshipService.sendFriendRequest(
            currentUser.getId(), addresseeId, request.getAlias());
        
        return ResponseEntity.ok(new FriendRequestResponse(
            "친구 요청이 성공적으로 전송되었습니다.",
            friendship.getId(),
            LocalDateTime.now()
        ));
    }
    
    // Helper methods
    private FriendDto convertToFriendDto(Friendship friendship, Member currentUser) {
        Member friend = friendship.getOtherMember(currentUser);
        String alias = friendship.getAliasForMember(currentUser);
        
        return new FriendDto(
            friendship.getId(),
            friend.getId(),
            friend.getUsername(),
            friend.getName(),
            alias,
            friendship.getStatus().name(),
            friendship.getAcceptedAt(),
            friendship.getLastInteractionAt()
        );
    }
    
    private FriendRequestDto convertToFriendRequestDto(Friendship friendship) {
        return new FriendRequestDto(
            friendship.getId(),
            friendship.getRequester().getId(),
            friendship.getRequester().getUsername(),
            friendship.getRequester().getName(),
            friendship.getAddressee().getId(),
            friendship.getAddressee().getUsername(),
            friendship.getAddressee().getName(),
            friendship.getRequesterAlias(),
            friendship.getStatus().name(),
            friendship.getRequestedAt()
        );
    }
    
    // DTO Classes
    
    @Data
    @Schema(description = "친구 요청 수락 DTO")
    public static class FriendAcceptDto {
        @Schema(description = "친구 별칭", example = "딸")
        private String alias;
    }
    
    @Data
    @Schema(description = "친구 별칭 설정 DTO")
    public static class FriendAliasDto {
        @NotBlank(message = "별칭은 필수입니다.")
        @Schema(description = "친구 별칭", example = "엄마")
        private String alias;
    }
    
    @Data
    @Schema(description = "친구 정보 DTO")
    public static class FriendDto {
        private Long friendshipId;
        private Long friendId;
        private String username;
        private String name;
        private String alias;
        private String status;
        private LocalDateTime acceptedAt;
        private LocalDateTime lastInteractionAt;
        
        public FriendDto(Long friendshipId, Long friendId, String username, String name, 
                        String alias, String status, LocalDateTime acceptedAt, LocalDateTime lastInteractionAt) {
            this.friendshipId = friendshipId;
            this.friendId = friendId;
            this.username = username;
            this.name = name;
            this.alias = alias;
            this.status = status;
            this.acceptedAt = acceptedAt;
            this.lastInteractionAt = lastInteractionAt;
        }
    }
    
    @Data
    @Schema(description = "친구 요청 정보 DTO")
    public static class FriendRequestDto {
        private Long friendshipId;
        private Long requesterId;
        private String requesterUsername;
        private String requesterName;
        private Long addresseeId;
        private String addresseeUsername;
        private String addresseeName;
        private String requesterAlias;
        private String status;
        private LocalDateTime requestedAt;
        
        public FriendRequestDto(Long friendshipId, Long requesterId, String requesterUsername, 
                               String requesterName, Long addresseeId, String addresseeUsername,
                               String addresseeName, String requesterAlias, String status, 
                               LocalDateTime requestedAt) {
            this.friendshipId = friendshipId;
            this.requesterId = requesterId;
            this.requesterUsername = requesterUsername;
            this.requesterName = requesterName;
            this.addresseeId = addresseeId;
            this.addresseeUsername = addresseeUsername;
            this.addresseeName = addresseeName;
            this.requesterAlias = requesterAlias;
            this.status = status;
            this.requestedAt = requestedAt;
        }
    }
    
    @Data
    @Schema(description = "친구 요청 수신 DTO")
    public static class FriendRequestInputDto {
        @NotNull(message = "대상자 ID는 필수입니다.")
        @Schema(description = "친구 요청 대상자 ID")
        private Long addresseeId;
        
        @Schema(description = "친구 별칭", example = "아빠")
        private String alias;
    }
    
    @Data
    @Schema(description = "초대 코드로 친구 요청 DTO")
    public static class FriendRequestByCodeDto {
        @NotBlank(message = "초대 코드는 필수입니다.")
        @Schema(description = "친구 초대 코드", example = "12345678")
        private String inviteCode;
        
        @Schema(description = "친구 별칭", example = "친구")
        private String alias;
    }
    
    @Data
    @Schema(description = "친구 초대 코드 응답 DTO")
    public static class FriendInviteCodeResponse {
        private String code;
        private long ttlSeconds;
        
        public FriendInviteCodeResponse(String code, long ttlSeconds) {
            this.code = code;
            this.ttlSeconds = ttlSeconds;
        }
    }
    
    
    @Data
    @Schema(description = "친구 상태 DTO")
    public static class FriendStatusDto {
        private long friendCount;
        private long pendingRequestCount;
        
        public FriendStatusDto(long friendCount, long pendingRequestCount) {
            this.friendCount = friendCount;
            this.pendingRequestCount = pendingRequestCount;
        }
    }
    
    @Data
    @Schema(description = "친구 요청 응답 DTO")
    public static class FriendRequestResponse {
        private String message;
        private Long friendshipId;
        private LocalDateTime timestamp;
        
        public FriendRequestResponse(String message, Long friendshipId, LocalDateTime timestamp) {
            this.message = message;
            this.friendshipId = friendshipId;
            this.timestamp = timestamp;
        }
    }
    
    @Data
    @Schema(description = "친구 목록 응답 DTO")
    public static class FriendListResponse {
        private List<FriendDto> friends;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int size;
        
        public FriendListResponse(List<FriendDto> friends, long totalElements, 
                                 int totalPages, int currentPage, int size) {
            this.friends = friends;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }
    }
    
    @Data
    @Schema(description = "친구 요청 목록 응답 DTO")
    public static class FriendRequestListResponse {
        private List<FriendRequestDto> requests;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int size;
        
        public FriendRequestListResponse(List<FriendRequestDto> requests, long totalElements,
                                        int totalPages, int currentPage, int size) {
            this.requests = requests;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }
    }
}