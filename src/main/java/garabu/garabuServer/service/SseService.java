package garabu.garabuServer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import garabu.garabuServer.domain.UserBook;
import garabu.garabuServer.service.UserBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {
    
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30분
    private final ObjectMapper objectMapper;
    private final UserBookService userBookService;
    
    // 가계부별 구독자 관리 (bookId -> List<EmitterInfo>)
    private final Map<Long, CopyOnWriteArrayList<EmitterInfo>> emitters = new ConcurrentHashMap<>();
    
    // 사용자별 구독 관리 (userId -> List<EmitterInfo>)
    private final Map<Long, CopyOnWriteArrayList<EmitterInfo>> userEmitters = new ConcurrentHashMap<>();
    
    /**
     * SSE 구독
     */
    public SseEmitter subscribe(Long bookId, Long userId, String lastEventId) {
        // 사용자의 가계부 접근 권한 확인
        Optional<UserBook> userBook = userBookService.findByBookIdAndMemberId(bookId, userId);
        if (userBook.isEmpty()) {
            log.error("SSE 구독 실패 - 권한 없음: 사용자 {}, 가계부 {}", userId, bookId);
            SseEmitter emitter = new SseEmitter(0L);
            emitter.completeWithError(new IllegalStateException("해당 가계부에 접근 권한이 없습니다."));
            return emitter;
        }
        
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        EmitterInfo emitterInfo = new EmitterInfo(emitter, bookId, userId);
        
        // 가계부별 구독자 추가
        emitters.computeIfAbsent(bookId, k -> new CopyOnWriteArrayList<>()).add(emitterInfo);
        
        // 사용자별 구독 추가
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitterInfo);
        
        // 콜백 설정
        emitter.onCompletion(() -> removeEmitter(emitterInfo));
        emitter.onTimeout(() -> removeEmitter(emitterInfo));
        emitter.onError((e) -> removeEmitter(emitterInfo));
        
        // 초기 연결 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data(Map.of(
                    "message", "SSE 연결 성공",
                    "bookId", bookId,
                    "userId", userId
                )));
        } catch (IOException e) {
            log.error("초기 이벤트 전송 실패", e);
            removeEmitter(emitterInfo);
        }
        
        // lastEventId가 있으면 놓친 이벤트 재전송
        if (lastEventId != null) {
            // TODO: 이벤트 저장소에서 놓친 이벤트 조회 및 전송
        }
        
        return emitter;
    }
    
    /**
     * 가계부 업데이트 이벤트 전송
     */
    public void sendBookUpdateEvent(Long bookId, String eventType, Object data) {
        CopyOnWriteArrayList<EmitterInfo> bookEmitters = emitters.get(bookId);
        
        if (bookEmitters == null || bookEmitters.isEmpty()) {
            return;
        }
        
        SseEvent event = new SseEvent(eventType, data, System.currentTimeMillis());
        
        bookEmitters.forEach(emitterInfo -> {
            try {
                emitterInfo.emitter.send(SseEmitter.event()
                    .name(eventType)
                    .id(String.valueOf(event.timestamp))
                    .data(event));
            } catch (IOException e) {
                log.error("이벤트 전송 실패: {}", e.getMessage());
                removeEmitter(emitterInfo);
            }
        });
    }
    
    /**
     * 특정 사용자에게 이벤트 전송
     */
    public void sendUserEvent(Long userId, String eventType, Object data) {
        CopyOnWriteArrayList<EmitterInfo> userEmitterList = userEmitters.get(userId);
        
        if (userEmitterList == null || userEmitterList.isEmpty()) {
            return;
        }
        
        SseEvent event = new SseEvent(eventType, data, System.currentTimeMillis());
        
        userEmitterList.forEach(emitterInfo -> {
            try {
                emitterInfo.emitter.send(SseEmitter.event()
                    .name(eventType)
                    .id(String.valueOf(event.timestamp))
                    .data(event));
            } catch (IOException e) {
                log.error("사용자 이벤트 전송 실패: {}", e.getMessage());
                removeEmitter(emitterInfo);
            }
        });
    }
    
    /**
     * Heartbeat 전송
     */
    public void sendHeartbeat(Long bookId, Long userId) {
        CopyOnWriteArrayList<EmitterInfo> bookEmitters = emitters.get(bookId);
        
        if (bookEmitters != null) {
            bookEmitters.stream()
                .filter(info -> info.userId.equals(userId))
                .forEach(emitterInfo -> {
                    try {
                        emitterInfo.emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data(Map.of("timestamp", System.currentTimeMillis())));
                    } catch (IOException e) {
                        removeEmitter(emitterInfo);
                    }
                });
        }
    }
    
    /**
     * 모든 연결에 heartbeat 전송 (주기적으로 실행)
     */
    @Scheduled(fixedDelay = 30000) // 30초마다
    public void sendHeartbeatToAll() {
        emitters.values().forEach(emitterList -> 
            emitterList.forEach(emitterInfo -> {
                try {
                    emitterInfo.emitter.send(SseEmitter.event()
                        .comment("heartbeat"));
                } catch (IOException e) {
                    removeEmitter(emitterInfo);
                }
            })
        );
    }
    
    /**
     * Emitter 제거
     */
    private void removeEmitter(EmitterInfo emitterInfo) {
        CopyOnWriteArrayList<EmitterInfo> bookEmitters = emitters.get(emitterInfo.bookId);
        if (bookEmitters != null) {
            bookEmitters.remove(emitterInfo);
            if (bookEmitters.isEmpty()) {
                emitters.remove(emitterInfo.bookId);
            }
        }
        
        CopyOnWriteArrayList<EmitterInfo> userEmitterList = userEmitters.get(emitterInfo.userId);
        if (userEmitterList != null) {
            userEmitterList.remove(emitterInfo);
            if (userEmitterList.isEmpty()) {
                userEmitters.remove(emitterInfo.userId);
            }
        }
        
        log.debug("SSE 연결 종료 - 사용자: {}, 가계부: {}", emitterInfo.userId, emitterInfo.bookId);
    }
    
    /**
     * 활성 연결 수 조회
     */
    public int getActiveConnectionCount() {
        return emitters.values().stream()
            .mapToInt(CopyOnWriteArrayList::size)
            .sum();
    }
    
    /**
     * EmitterInfo 내부 클래스
     */
    private static class EmitterInfo {
        final SseEmitter emitter;
        final Long bookId;
        final Long userId;
        
        EmitterInfo(SseEmitter emitter, Long bookId, Long userId) {
            this.emitter = emitter;
            this.bookId = bookId;
            this.userId = userId;
        }
    }
    
    /**
     * SSE 이벤트 DTO
     */
    public static class SseEvent {
        public final String type;
        public final Object data;
        public final Long timestamp;
        
        public SseEvent(String type, Object data, Long timestamp) {
            this.type = type;
            this.data = data;
            this.timestamp = timestamp;
        }
    }
} 