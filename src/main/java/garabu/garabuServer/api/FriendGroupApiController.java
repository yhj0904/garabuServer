package garabu.garabuServer.api;

import garabu.garabuServer.domain.FriendGroup;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.service.FriendGroupService;
import garabu.garabuServer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import java.util.stream.Collectors;

/**
 * 친구 그룹 API 컨트롤러
 */
@RestController
@RequestMapping("/api/v2/friend-groups")
@RequiredArgsConstructor
@Tag(name = "FriendGroup", description = "친구 그룹 API")
@SecurityRequirement(name = "bearerAuth")
public class FriendGroupApiController {
    
    private final FriendGroupService friendGroupService;
    private final MemberService memberService;
    
    /**
     * 친구 그룹 생성
     */
    @PostMapping
    @Operation(summary = "친구 그룹 생성", description = "새로운 친구 그룹을 생성합니다.")
    public ResponseEntity<FriendGroupResponse> createFriendGroup(
            @Valid @RequestBody CreateFriendGroupRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        FriendGroup friendGroup = friendGroupService.createFriendGroup(
            currentUser.getId(),
            request.getName(),
            request.getDescription(),
            request.getColor(),
            request.getIcon()
        );
        
        return ResponseEntity.ok(new FriendGroupResponse(
            "친구 그룹이 생성되었습니다.",
            friendGroup.getId(),
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 그룹 목록 조회
     */
    @GetMapping
    @Operation(summary = "친구 그룹 목록 조회", description = "현재 사용자의 친구 그룹 목록을 조회합니다.")
    public ResponseEntity<FriendGroupListResponse> getFriendGroups(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Page<FriendGroup> friendGroups = friendGroupService.getFriendGroups(currentUser.getId(), pageable);
        
        List<FriendGroupDto> groups = friendGroups.getContent().stream()
            .map(this::convertToFriendGroupDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new FriendGroupListResponse(
            groups,
            friendGroups.getTotalElements(),
            friendGroups.getTotalPages(),
            friendGroups.getNumber(),
            friendGroups.getSize()
        ));
    }
    
    /**
     * 친구 그룹 수정
     */
    @PutMapping("/{groupId}")
    @Operation(summary = "친구 그룹 수정", description = "친구 그룹 정보를 수정합니다.")
    public ResponseEntity<FriendGroupResponse> updateFriendGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateFriendGroupRequest request) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        friendGroupService.updateFriendGroup(
            groupId,
            currentUser.getId(),
            request.getName(),
            request.getDescription(),
            request.getColor(),
            request.getIcon()
        );
        
        return ResponseEntity.ok(new FriendGroupResponse(
            "친구 그룹이 수정되었습니다.",
            groupId,
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 그룹 삭제
     */
    @DeleteMapping("/{groupId}")
    @Operation(summary = "친구 그룹 삭제", description = "친구 그룹을 삭제합니다.")
    public ResponseEntity<FriendGroupResponse> deleteFriendGroup(@PathVariable Long groupId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        friendGroupService.deleteFriendGroup(groupId, currentUser.getId());
        
        return ResponseEntity.ok(new FriendGroupResponse(
            "친구 그룹이 삭제되었습니다.",
            groupId,
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 그룹에 친구 추가
     */
    @PostMapping("/{groupId}/members/{friendshipId}")
    @Operation(summary = "친구 그룹에 친구 추가", description = "친구 그룹에 친구를 추가합니다.")
    public ResponseEntity<FriendGroupResponse> addFriendToGroup(
            @PathVariable Long groupId,
            @PathVariable Long friendshipId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        friendGroupService.addFriendToGroup(groupId, friendshipId, currentUser.getId());
        
        return ResponseEntity.ok(new FriendGroupResponse(
            "친구가 그룹에 추가되었습니다.",
            groupId,
            LocalDateTime.now()
        ));
    }
    
    /**
     * 친구 그룹에서 친구 제거
     */
    @DeleteMapping("/{groupId}/members/{friendshipId}")
    @Operation(summary = "친구 그룹에서 친구 제거", description = "친구 그룹에서 친구를 제거합니다.")
    public ResponseEntity<FriendGroupResponse> removeFriendFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long friendshipId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        friendGroupService.removeFriendFromGroup(groupId, friendshipId, currentUser.getId());
        
        return ResponseEntity.ok(new FriendGroupResponse(
            "친구가 그룹에서 제거되었습니다.",
            groupId,
            LocalDateTime.now()
        ));
    }
    
    private FriendGroupDto convertToFriendGroupDto(FriendGroup friendGroup) {
        return new FriendGroupDto(
            friendGroup.getId(),
            friendGroup.getName(),
            friendGroup.getDescription(),
            friendGroup.getColor(),
            friendGroup.getIcon(),
            friendGroup.getMemberCount(),
            friendGroup.getCreatedAt(),
            friendGroup.getUpdatedAt()
        );
    }
    
    // DTO Classes
    @Data
    public static class CreateFriendGroupRequest {
        @NotBlank(message = "그룹명은 필수입니다.")
        private String name;
        private String description;
        private String color;
        private String icon;
    }
    
    @Data
    public static class UpdateFriendGroupRequest {
        @NotBlank(message = "그룹명은 필수입니다.")
        private String name;
        private String description;
        private String color;
        private String icon;
    }
    
    @Data
    public static class FriendGroupDto {
        private Long id;
        private String name;
        private String description;
        private String color;
        private String icon;
        private int memberCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public FriendGroupDto(Long id, String name, String description, String color, String icon,
                            int memberCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.color = color;
            this.icon = icon;
            this.memberCount = memberCount;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
    
    @Data
    public static class FriendGroupResponse {
        private String message;
        private Long groupId;
        private LocalDateTime timestamp;
        
        public FriendGroupResponse(String message, Long groupId, LocalDateTime timestamp) {
            this.message = message;
            this.groupId = groupId;
            this.timestamp = timestamp;
        }
    }
    
    @Data
    public static class FriendGroupListResponse {
        private List<FriendGroupDto> groups;
        private long totalElements;
        private int totalPages;
        private int currentPage;
        private int size;
        
        public FriendGroupListResponse(List<FriendGroupDto> groups, long totalElements,
                                     int totalPages, int currentPage, int size) {
            this.groups = groups;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }
    }
}