# SSE (Server-Sent Events) 마이그레이션 가이드

## 개요

가라부 프로젝트는 실시간 통신을 WebSocket에서 SSE + Redis Pub/Sub로 전환했습니다.

### 전환 이유
- **서버 부하 감소**: HTTP 기반으로 연결 유지 비용이 적음
- **안정성 향상**: 프록시/방화벽 친화적
- **자동 재연결**: EventSource의 내장 재연결 기능
- **확장성**: Redis Pub/Sub을 통한 다중 서버 지원

## 아키텍처

```
┌─────────────┐     SSE Connection    ┌─────────────┐
│   Client    │ ←─────────────────── │  SSE Server │
│ (EventSource)│                      │             │
└──────┬──────┘                      └──────┬──────┘
       │                                     │
       │         REST API                    │ Redis
       └────────────────────>                │ Pub/Sub
                                            ▼
                                    ┌───────────────┐
                                    │    Redis      │
                                    │    Broker     │
                                    └───────────────┘
```

## 서버 구현

### 1. SSE 엔드포인트

```java
@RestController
@RequestMapping("/api/v2/sse")
public class SseApiController {
    
    @GetMapping(value = "/subscribe/{bookId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @PathVariable Long bookId,
            @AuthenticationPrincipal Member member,
            @RequestParam(value = "token", required = false) String tokenParam) {
        
        return sseService.subscribe(bookId, member.getId(), null);
    }
}
```

### 2. 이벤트 발행

```java
// API에서 이벤트 발행
BookEvent event = BookEvent.ledgerCreated(bookId, userId, ledgerData);
bookEventPublisher.publishBookEvent(event);
```

### 3. Redis 이벤트 구독 및 SSE 전송

```java
@Component
public class BookEventListener implements MessageListener {
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        BookEvent event = // deserialize message
        sseService.sendBookUpdateEvent(event.getBookId(), event.getEventType(), event);
    }
}
```

## 클라이언트 구현

### React Native (TypeScript)

```typescript
const eventSource = new EventSource(
    `${API_BASE_URL}/api/v2/sse/subscribe/${bookId}?token=${token}`
);

eventSource.addEventListener('LEDGER_CREATED', (event) => {
    const data = JSON.parse(event.data);
    // 상태 업데이트
});

eventSource.onerror = () => {
    // 자동 재연결됨
};
```

### React Web

```typescript
const { isConnected, sendUpdate } = useSse(bookId, {
    onMessage: (event) => {
        console.log('이벤트 수신:', event);
    }
});
```

## 이벤트 타입

### 가계부 이벤트
- `LEDGER_CREATED`: 거래 생성
- `LEDGER_UPDATED`: 거래 수정
- `LEDGER_DELETED`: 거래 삭제
- `MEMBER_ADDED`: 멤버 추가
- `MEMBER_REMOVED`: 멤버 제거
- `MEMBER_UPDATED`: 멤버 권한 변경
- `BOOK_CREATED`: 가계부 생성
- `BOOK_UPDATED`: 가계부 수정

### 시스템 이벤트
- `connected`: 연결 성공
- `heartbeat`: 연결 유지 확인

## 보안

### 인증
- JWT Bearer 토큰 지원
- URL 쿼리 파라미터로도 토큰 전달 가능 (EventSource 제약 대응)

### 권한 체크
- 가계부 접근 권한 확인
- UserBook 관계 검증

## 모니터링

### 연결 수 확인
```java
int activeConnections = sseService.getActiveConnectionCount();
```

### 로그
- 연결/해제: INFO 레벨
- 이벤트 전송: DEBUG 레벨
- 오류: ERROR 레벨

## 마이그레이션 체크리스트

- [ ] WebSocket 코드 제거
- [ ] SSE 엔드포인트 설정
- [ ] Redis Pub/Sub 설정
- [ ] 클라이언트 코드 수정
- [ ] 인증 방식 확인
- [ ] 모니터링 설정
- [ ] 부하 테스트

## 주의사항

1. **단방향 통신**: SSE는 서버→클라이언트 단방향입니다. 클라이언트→서버는 REST API를 사용하세요.
2. **연결 제한**: 브라우저당 SSE 연결은 6개로 제한됩니다.
3. **타임아웃**: 30분 타임아웃 설정, heartbeat로 연결 유지
4. **메시지 크기**: 대용량 데이터는 별도 API로 조회

## 트러블슈팅

### 연결이 안 될 때
1. 토큰 유효성 확인
2. 가계부 접근 권한 확인
3. CORS 설정 확인

### 이벤트를 받지 못할 때
1. Redis 연결 상태 확인
2. 이벤트 타입명 확인
3. 로그 레벨 DEBUG로 설정하여 확인 