package garabu.garabuServer.api;

import garabu.garabuServer.domain.Message;
import garabu.garabuServer.domain.MessageType;
import garabu.garabuServer.domain.Member;
import garabu.garabuServer.domain.Friendship;
import garabu.garabuServer.repository.MessageRepository;
import garabu.garabuServer.repository.FriendshipRepository;
import garabu.garabuServer.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/messages")
@RequiredArgsConstructor
@Tag(name = "Message", description = "메시지 API")
@SecurityRequirement(name = "bearerAuth")
public class MessageApiController {
    
    private final MessageRepository messageRepository;
    private final FriendshipRepository friendshipRepository;
    private final MemberService memberService;
    
    @PostMapping
    @Operation(summary = "메시지 전송", description = "친구에게 메시지를 전송합니다. 친구 관계가 성립된 사용자에게만 메시지를 전송할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메시지 전송 성공",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "친구 관계 또는 수신자를 찾을 수 없음")
    })
    @Transactional
    public ResponseEntity<MessageResponse> sendMessage(
            @Parameter(description = "메시지 전송 요청", required = true) @Valid @RequestBody SendMessageRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        Member receiver = memberService.findById(request.getReceiverId());
        
        Friendship friendship = friendshipRepository.findByRequesterAndAddressee(currentUser, receiver)
            .orElseThrow(() -> new IllegalArgumentException("친구 관계가 없습니다."));
        
        Message message = new Message();
        message.setSender(currentUser);
        message.setReceiver(receiver);
        message.setFriendship(friendship);
        message.setMessageType(MessageType.TEXT);
        message.setContent(request.getContent());
        
        Message savedMessage = messageRepository.save(message);
        
        return ResponseEntity.ok(new MessageResponse(
            savedMessage.getId(),
            "메시지가 전송되었습니다.",
            LocalDateTime.now()
        ));
    }
    
    @GetMapping("/conversation/{friendshipId}")
    @Operation(summary = "대화 내역 조회", description = "특정 친구와의 대화 내역을 페이지 단위로 조회합니다. 최신 메시지가 먼저 표시됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "대화 내역 조회 성공",
            content = @Content(schema = @Schema(implementation = MessageListResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "친구 관계를 찾을 수 없음")
    })
    public ResponseEntity<MessageListResponse> getConversation(
            @Parameter(description = "친구 관계 ID", required = true) @PathVariable Long friendshipId,
            @Parameter(description = "페이지네이션 정보") @PageableDefault(size = 20) Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Friendship friendship = friendshipRepository.findById(friendshipId)
            .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));
        
        Page<Message> messages = messageRepository.findByFriendshipAndMember(friendship, currentUser, pageable);
        
        List<MessageDto> messageDtos = messages.getContent().stream()
            .map(this::convertToMessageDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new MessageListResponse(
            messageDtos,
            messages.getTotalElements(),
            messages.getTotalPages(),
            messages.getNumber(),
            messages.getSize()
        ));
    }
    
    @PostMapping("/{messageId}/read")
    @Operation(summary = "메시지 읽음 처리", description = "메시지를 읽음으로 처리합니다. 본인이 수신한 메시지만 읽음 처리할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메시지 읽음 처리 성공",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "메시지 읽음 처리 권한 없음"),
        @ApiResponse(responseCode = "404", description = "메시지를 찾을 수 없음")
    })
    @Transactional
    public ResponseEntity<MessageResponse> markAsRead(
            @Parameter(description = "메시지 ID", required = true) @PathVariable Long messageId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Member currentUser = memberService.findMemberByUsername(auth.getName());
        
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));
        
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("메시지를 읽을 권한이 없습니다.");
        }
        
        message.markAsRead();
        messageRepository.save(message);
        
        return ResponseEntity.ok(new MessageResponse(
            messageId,
            "메시지가 읽음 처리되었습니다.",
            LocalDateTime.now()
        ));
    }
    
    private MessageDto convertToMessageDto(Message message) {
        return new MessageDto(
            message.getId(),
            message.getSender().getId(),
            message.getSender().getName(),
            message.getReceiver().getId(),
            message.getReceiver().getName(),
            message.getContent(),
            message.getMessageType().name(),
            message.getStatus().name(),
            message.getSentAt(),
            message.getReadAt()
        );
    }
    
    @Data
    @Schema(description = "메시지 전송 요청")
    public static class SendMessageRequest {
        @Schema(description = "수신자 ID", example = "123", required = true)
        private Long receiverId;
        @Schema(description = "메시지 내용", example = "안녕하세요!", required = true, minLength = 1, maxLength = 1000)
        private String content;
    }
    
    @Data
    @Schema(description = "메시지 정보")
    public static class MessageDto {
        @Schema(description = "메시지 ID", example = "1")
        private Long id;
        @Schema(description = "발신자 ID", example = "123")
        private Long senderId;
        @Schema(description = "발신자 이름", example = "홍길동")
        private String senderName;
        @Schema(description = "수신자 ID", example = "456")
        private Long receiverId;
        @Schema(description = "수신자 이름", example = "김철수")
        private String receiverName;
        @Schema(description = "메시지 내용", example = "안녕하세요!")
        private String content;
        @Schema(description = "메시지 타입", example = "TEXT", allowableValues = {"TEXT", "IMAGE", "FILE"})
        private String messageType;
        @Schema(description = "메시지 상태", example = "SENT", allowableValues = {"SENT", "DELIVERED", "READ"})
        private String status;
        @Schema(description = "발송 시간", example = "2024-01-15T10:30:00")
        private LocalDateTime sentAt;
        @Schema(description = "읽음 시간", example = "2024-01-15T10:35:00")
        private LocalDateTime readAt;
        
        public MessageDto(Long id, Long senderId, String senderName, Long receiverId, String receiverName,
                         String content, String messageType, String status, LocalDateTime sentAt, LocalDateTime readAt) {
            this.id = id;
            this.senderId = senderId;
            this.senderName = senderName;
            this.receiverId = receiverId;
            this.receiverName = receiverName;
            this.content = content;
            this.messageType = messageType;
            this.status = status;
            this.sentAt = sentAt;
            this.readAt = readAt;
        }
    }
    
    @Data
    @Schema(description = "메시지 작업 응답")
    public static class MessageResponse {
        @Schema(description = "메시지 ID", example = "1")
        private Long messageId;
        @Schema(description = "응답 메시지", example = "메시지가 전송되었습니다.")
        private String message;
        @Schema(description = "작업 시간", example = "2024-01-15T10:30:00")
        private LocalDateTime timestamp;
        
        public MessageResponse(Long messageId, String message, LocalDateTime timestamp) {
            this.messageId = messageId;
            this.message = message;
            this.timestamp = timestamp;
        }
    }
    
    @Data
    @Schema(description = "메시지 목록 응답")
    public static class MessageListResponse {
        @Schema(description = "메시지 목록")
        private List<MessageDto> messages;
        @Schema(description = "전체 메시지 수", example = "50")
        private long totalElements;
        @Schema(description = "전체 페이지 수", example = "3")
        private int totalPages;
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private int currentPage;
        @Schema(description = "페이지 크기", example = "20")
        private int size;
        
        public MessageListResponse(List<MessageDto> messages, long totalElements, 
                                 int totalPages, int currentPage, int size) {
            this.messages = messages;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
            this.size = size;
        }
    }
}