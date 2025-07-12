package garabu.garabuServer.config;

import garabu.garabuServer.jwt.JWTUtil;
import garabu.garabuServer.jwt.CustomUserDetails;
import garabu.garabuServer.domain.Member;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple message broker to send messages to clients
        config.enableSimpleBroker("/topic", "/queue");
        // Set prefix for messages that are bound for @MessageMapping-annotated methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint that clients will use to connect to WebSocket server
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = null;
                    
                    // Try to get token from native header (query parameter from handshake)
                    if (accessor.getNativeHeader("token") != null && !accessor.getNativeHeader("token").isEmpty()) {
                        token = accessor.getNativeHeader("token").get(0);
                    }
                    
                    // Try to get token from Authorization header
                    if (token == null && accessor.getNativeHeader("Authorization") != null && !accessor.getNativeHeader("Authorization").isEmpty()) {
                        String authHeader = accessor.getNativeHeader("Authorization").get(0);
                        if (authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                        }
                    }
                    
                    if (token != null) {
                        try {
                            // Validate token
                            if (!jwtUtil.isExpired(token)) {
                                String category = jwtUtil.getCategory(token);
                                if ("access".equals(category)) {
                                    String username = jwtUtil.getUsername(token);
                                    String role = jwtUtil.getRole(token);
                                    
                                    Member userEntity = new Member();
                                    userEntity.setUsername(username);
                                    userEntity.setRole(role);
                                    CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
                                    
                                    Authentication auth = new UsernamePasswordAuthenticationToken(
                                            customUserDetails, null, customUserDetails.getAuthorities());
                                    SecurityContextHolder.getContext().setAuthentication(auth);
                                    accessor.setUser(auth);
                                    
                                    log.debug("WebSocket authentication successful for user: {}", username);
                                } else {
                                    log.warn("Invalid token category: {}", category);
                                    throw new RuntimeException("Invalid access token");
                                }
                            } else {
                                log.warn("Expired JWT token");
                                throw new RuntimeException("Access token expired");
                            }
                        } catch (ExpiredJwtException e) {
                            log.warn("Expired JWT token: {}", e.getMessage());
                            throw new RuntimeException("Access token expired");
                        } catch (Exception e) {
                            log.warn("JWT token validation failed: {}", e.getMessage());
                            throw new RuntimeException("Authentication failed");
                        }
                    } else {
                        log.warn("No JWT token found in WebSocket connection");
                        throw new RuntimeException("Authentication required");
                    }
                }
                
                return message;
            }
        });
    }
}