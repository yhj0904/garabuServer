# 가라부 서버

가라부 서버는 Spring Boot 3으로 개발된 가계부 관리 백엔드입니다. OAuth2 소셜 로그인을 통해 사용자 인증을 처리하고 JWT(액세스/리프레시) 토큰으로 보호된 API를 제공합니다. Redis, MySQL, Firebase, ELK 스택, Prometheus/Grafana 등 다양한 도구를 연동하여 실사용 환경을 고려해 설계되었습니다.

## 주요 기능

- **OAuth2 로그인**: Google, Naver 등 외부 제공자를 이용하여 로그인합니다.
- **JWT 인증**: `LoginFilter`와 `CustomLogoutFilter`를 통해 토큰을 발급/폐기하며 `ReissueController`로 재발급을 지원합니다【F:src/main/java/garabu/garabuServer/controller/ReissueController.java†L1-L67】.
- **가계부 관리 API**: `BookApiController`, `LedgerApiController` 등에서 가계부, 가계 기록, 카테고리, 결제수단 등을 CRUD 합니다【F:src/main/java/garabu/garabuServer/api/BookApiController.java†L1-L35】【F:src/main/java/garabu/garabuServer/api/LedgerApiController.java†L1-L68】.
- **공동 가계부**: `UserBook` 엔티티로 여러 사용자가 하나의 가계부를 공유할 수 있습니다【F:src/main/java/garabu/garabuServer/domain/UserBook.java†L1-L23】.
- **푸시 알림**: `FcmSendServiceImpl`을 통해 FCM으로 푸시 메시지를 발송합니다【F:src/main/java/garabu/garabuServer/service/impl/FcmSendServiceImpl.java†L1-L74】.
- **모니터링/로그 수집**: Docker Compose로 Prometheus, Grafana, Elasticsearch, Logstash, Filebeat 구성을 제공합니다【F:docker-compose.yml†L1-L139】.

## 폴더 구조

```
src
 ├─ main
 │  ├─ java/garabu/garabuServer
 │  │  ├─ api               # REST API 컨트롤러
 │  │  ├─ config            # Security, CORS, Redis 등 설정
 │  │  ├─ controller        # 보조 API (토큰 재발급 등)
 │  │  ├─ domain            # JPA 엔티티
 │  │  ├─ dto               # 요청/응답 DTO
 │  │  ├─ jwt               # JWT 필터 및 유틸리티
 │  │  ├─ oauth2            # OAuth2 로그인 처리
 │  │  ├─ repository        # Spring Data JPA 인터페이스
 │  │  └─ service           # 비즈니스 로직
 │  └─ resources
 │     ├─ docker            # 컨테이너용 Dockerfile, compose 파일
 │     ├─ k8s               # Kubernetes 매니페스트 예제
 │     ├─ filebeat          # Filebeat 설정
 │     └─ logback-spring.xml# 로그 설정
 └─ test
    └─ java/garabu/garabuServer
       └─ service          # 단위 테스트 (일부 주석 처리됨)
```

## 빠른 시작

1. `application.yml`에 DB 접속 정보, `jwt.secret`, OAuth2 클라이언트 ID 등 필요한 값을 설정합니다.
2. 의존성을 설치하고 애플리케이션을 실행합니다.

```bash
./gradlew bootRun
```

3. API 문서는 `/swagger-ui/index.html`에서 확인할 수 있습니다.

### Docker 환경 실행

모니터링 및 로그 수집 도구를 함께 사용하려면 다음 명령으로 컨테이너를 기동합니다.

```bash
docker compose up --build
```

ELK 스택과 Prometheus, Grafana, Redis가 함께 실행됩니다.

## 테스트

`src/test/java`의 테스트 코드는 기본적인 서비스 로직을 검증합니다. 현재 일부 테스트가 주석 처리되어 있어 실행 시 주의가 필요합니다【F:src/test/java/garabu/garabuServer/service/MemberServiceTest.java†L1-L40】.

## 개선이 필요한 부분

- 예제 `application.yml`이 없어 초심자가 환경 구성을 어려워할 수 있습니다.
- 테스트 코드가 대부분 주석 처리되어 있어 자동화된 검증이 부족합니다.
- FCM, OAuth 등 민감한 설정 값 관리를 위해 환경 변수 또는 Vault 사용을 검토할 필요가 있습니다.
- CI/CD 파이프라인(Jenkins, GitHub Actions 등)을 도입하여 빌드와 배포를 자동화하면 좋습니다.

가라부 서버는 가계부 관리와 실시간 알림 기능을 모두 갖춘 학습용/실습용 프로젝트입니다. 추가 개선을 통해 보다 안정적인 서비스로 발전시킬 수 있습니다.
