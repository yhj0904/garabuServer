package garabu.garabuServer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
public class WebSocketController {

    @MessageMapping("/connect")
    @SendTo("/topic/notifications")
    public Map<String, Object> handleConnection(@Payload Map<String, Object> message, 
                                              SimpMessageHeaderAccessor headerAccessor,
                                              Principal principal) {
        String username = principal != null ? principal.getName() : "Anonymous";
        log.info("WebSocket connection established for user: {}", username);
        
        return Map.of(
            "type", "connection",
            "message", "Successfully connected to WebSocket",
            "user", username,
            "timestamp", System.currentTimeMillis()
        );
    }

    @MessageMapping("/book/{bookId}/notifications")
    @SendTo("/topic/book/{bookId}")
    public Map<String, Object> handleBookNotification(@Payload Map<String, Object> message,
                                                    Principal principal) {
        String username = principal != null ? principal.getName() : "Anonymous";
        log.info("Book notification from user: {} - {}", username, message);
        
        return Map.of(
            "type", "book_update",
            "user", username,
            "data", message,
            "timestamp", System.currentTimeMillis()
        );
    }

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, Object> handlePing(@Payload Map<String, Object> message,
                                        Principal principal) {
        String username = principal != null ? principal.getName() : "Anonymous";
        
        return Map.of(
            "type", "pong",
            "user", username,
            "originalMessage", message,
            "timestamp", System.currentTimeMillis()
        );
    }
}