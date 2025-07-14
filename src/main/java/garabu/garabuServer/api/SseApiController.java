package garabu.garabuServer.api;

import garabu.garabuServer.domain.Member;
import garabu.garabuServer.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/v2/sse")
@RequiredArgsConstructor
public class SseApiController {
    
    private final SseService sseService;
    
    /**
     * SSE 연결 엔드포인트
     * 클라이언트가 특정 가계부의 실시간 업데이트를 구독
     */
    @GetMapping(value = "/subscribe/{bookId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Member member,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
            @RequestParam(value = "token", required = false) String tokenParam) {
        
        // EventSource는 헤더 설정이 제한적이므로 URL 파라미터로도 토큰을 받을 수 있도록 함
        // 이미 Spring Security에서 인증이 완료된 상태이므로 member가 null이 아님
        
        if (member == null) {
            log.error("SSE 구독 실패 - 인증되지 않은 사용자");
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new IllegalStateException("Unauthorized"));
            return emitter;
        }
        
        log.info("SSE 구독 요청 - 사용자: {}, 가계부: {}", member.getUsername(), bookId);
        
        return sseService.subscribe(bookId, member.getId(), lastEventId);
    }
    
    /**
     * 연결 상태 확인 (heartbeat)
     */
    @PostMapping("/heartbeat/{bookId}")
    public void heartbeat(@PathVariable Long bookId, @AuthenticationPrincipal Member member) {
        sseService.sendHeartbeat(bookId, member.getId());
    }
} 