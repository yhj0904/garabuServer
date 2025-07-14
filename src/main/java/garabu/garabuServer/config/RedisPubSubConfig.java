package garabu.garabuServer.config;

import garabu.garabuServer.event.BookEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class RedisPubSubConfig {
    
    private final BookEventListener bookEventListener;
    
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 가계부 이벤트 구독
        container.addMessageListener(
            new MessageListenerAdapter(bookEventListener),
            new PatternTopic("book-events:*")
        );
        
        // 사용자 이벤트 구독
        container.addMessageListener(
            new MessageListenerAdapter(bookEventListener),
            new PatternTopic("user-events:*")
        );
        
        // 브로드캐스트 이벤트 구독
        container.addMessageListener(
            new MessageListenerAdapter(bookEventListener),
            new PatternTopic("broadcast-events")
        );
        
        return container;
    }
} 