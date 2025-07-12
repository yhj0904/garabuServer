# WebSocket Authentication Fix

## Problem
The WebSocket endpoint `/ws` was being redirected to login instead of allowing connections with valid JWT tokens. The issue was that:

1. No WebSocket configuration existed
2. JWTFilter only handled Authorization headers (Bearer tokens) but WebSocket clients were passing tokens as query parameters
3. SecurityConfig didn't allow WebSocket endpoints
4. No WebSocket dependencies were included

## Solution

### 1. Added WebSocket Dependency
Added `spring-boot-starter-websocket` to `build.gradle` to enable WebSocket support.

### 2. Created WebSocket Configuration (`WebSocketConfig.java`)
- Enables STOMP messaging protocol
- Configures `/ws` endpoint with SockJS fallback
- Implements JWT authentication in `configureClientInboundChannel()`
- Validates JWT tokens from both `token` query parameter and `Authorization` header
- Sets up proper Spring Security context for authenticated users

### 3. Modified JWTFilter
- Updated to handle JWT tokens from query parameters for WebSocket connections
- Added special handling for `/ws` endpoints
- Maintains backward compatibility for regular HTTP requests

### 4. Updated SecurityConfig
- Added `/ws/**` to `permitAll()` matchers to allow WebSocket handshake
- Authentication is handled by WebSocket configuration, not Spring Security filters

### 5. Created WebSocketController
- Provides basic message handling endpoints
- Includes connection establishment, book notifications, and ping/pong functionality
- Properly extracts authenticated user information

## Usage

### Client Connection
```javascript
// Connect with JWT token as query parameter
const socket = new SockJS('/ws?token=YOUR_JWT_TOKEN&userId=4&bookId=10');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to topics
    stompClient.subscribe('/topic/notifications', function(message) {
        console.log('Received:', JSON.parse(message.body));
    });
    
    // Send messages
    stompClient.send('/app/connect', {}, JSON.stringify({
        message: 'Hello from client'
    }));
});
```

### Available Endpoints
- `/app/connect` - Connection establishment
- `/app/book/{bookId}/notifications` - Book-specific notifications
- `/app/ping` - Health check/ping

### Topics
- `/topic/notifications` - General notifications
- `/topic/book/{bookId}` - Book-specific updates
- `/topic/pong` - Ping responses

## Security Features
- JWT token validation on connection
- User authentication context maintained throughout session
- Role-based access control integration
- Secure token handling via query parameters or headers

## Testing
The WebSocket endpoint should now accept connections with valid JWT tokens and no longer redirect to login.