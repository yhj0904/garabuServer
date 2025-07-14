package garabu.garabuServer.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookEventPublisher {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 가계부 관련 이벤트 발행
     */
    public void publishBookEvent(BookEvent event) {
        String channel = "book-events:" + event.getBookId();
        log.debug("Redis 이벤트 발행 - 채널: {}, 이벤트: {}", channel, event.getEventType());
        redisTemplate.convertAndSend(channel, event);
    }
    
    /**
     * 사용자별 이벤트 발행
     */
    public void publishUserEvent(UserEvent event) {
        String channel = "user-events:" + event.getUserId();
        log.debug("사용자 이벤트 발행 - 채널: {}, 이벤트: {}", channel, event.getEventType());
        redisTemplate.convertAndSend(channel, event);
    }
    
    /**
     * 전체 브로드캐스트 이벤트
     */
    public void publishBroadcastEvent(BroadcastEvent event) {
        String channel = "broadcast-events";
        log.debug("브로드캐스트 이벤트 발행 - 이벤트: {}", event.getEventType());
        redisTemplate.convertAndSend(channel, event);
    }
} 