package garabu.garabuServer.event;

import garabu.garabuServer.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookEventListener implements MessageListener {
    
    private final SseService sseService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            Object event = redisTemplate.getValueSerializer().deserialize(message.getBody());
            
            log.debug("Redis 이벤트 수신 - 채널: {}", channel);
            
            if (event instanceof BookEvent) {
                handleBookEvent((BookEvent) event);
            } else if (event instanceof UserEvent) {
                handleUserEvent((UserEvent) event);
            } else if (event instanceof BroadcastEvent) {
                handleBroadcastEvent((BroadcastEvent) event);
            }
        } catch (Exception e) {
            log.error("이벤트 처리 중 오류 발생", e);
        }
    }
    
    private void handleBookEvent(BookEvent event) {
        log.info("가계부 이벤트 처리 - 타입: {}, 가계부: {}", event.getEventType(), event.getBookId());
        
        // SSE로 해당 가계부 구독자들에게 전송
        sseService.sendBookUpdateEvent(
            event.getBookId(), 
            event.getEventType(), 
            event
        );
    }
    
    private void handleUserEvent(UserEvent event) {
        log.info("사용자 이벤트 처리 - 타입: {}, 사용자: {}", event.getEventType(), event.getUserId());
        
        // SSE로 해당 사용자에게 전송
        sseService.sendUserEvent(
            event.getUserId(),
            event.getEventType(),
            event
        );
    }
    
    private void handleBroadcastEvent(BroadcastEvent event) {
        log.info("브로드캐스트 이벤트 처리 - 타입: {}", event.getEventType());
        
        // TODO: 모든 연결된 사용자에게 전송
    }
} 