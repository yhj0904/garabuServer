# 가라부 서버

가라부 서버는 Spring Boot 3 기반으로 구현된 가계부 관리용 백엔드 애플리케이션입니다. Google, Naver 등 OAuth2 제공자를 통해 로그인한 뒤 JWT 토큰을 이용해 보호된 REST API에 접근할 수 있습니다. 빌드 도구로는 Gradle을 사용하며, Spring Security와 JPA를 기본으로 합니다. 기본 설정은 인메모리 H2 데이터베이스를 사용하지만 MySQL로 손쉽게 전환할 수 있습니다.

## 주요 기능

- OAuth2 로그인 후 JWT(액세스/리프레시 토큰) 기반 인증
- 회원, 가계부(Book), 가계 기록(Ledger), 카테고리, 결제수단 관리 API 제공
- 리프레시 토큰을 DB에 저장하여 토큰 재발급 및 로그아웃 처리
- `UserBook` 엔티티를 통한 여러 사용자의 가계부 공동 관리 기능

## 패키지 구조

- `garabu.garabuServer.config` – CORS 설정과 필터 체인 등 보안 설정
- `garabu.garabuServer.jwt` – JWT 유틸리티 및 로그인·로그아웃 필터
- `garabu.garabuServer.oauth2` – OAuth2 로그인 성공 핸들러
- `garabu.garabuServer.domain` – JPA 엔티티 (`Member`, `Book`, `Ledger`, `Category`, `PaymentMethod`, `RefreshEntity`, `UserBook` 등)
- `garabu.garabuServer.repository` – Spring Data JPA 레포지토리
- `garabu.garabuServer.service` – 각 도메인별 서비스 로직
- `garabu.garabuServer.api` – 주요 기능을 제공하는 REST 컨트롤러
- `garabu.garabuServer.controller` – 토큰 재발급/현재 사용자 정보 등 보조 API

## 예시 API 엔드포인트

- `POST /join` – 회원 가입
- `POST /api/v2/book` – 가계부 생성
- `GET /api/v2/book/mybooks` – 내 가계부 목록 조회
- `POST /api/v2/ledger` – 수입·지출 기록 등록
- `POST /reissue` – 리프레시 토큰을 이용해 JWT 재발급

세부 엔드포인트와 파라미터는 `api` 패키지를 확인하세요.

## 실행 방법

`application.yml` 파일(저장소에는 포함되어 있지 않음)에 데이터베이스 접속 정보와 `jwt.secret` 등의 설정 값을 작성한 후 다음 명령어로 서버를 실행합니다.

```bash
./gradlew bootRun
```

`build.gradle` 기본 설정은 H2 데이터베이스를 사용하며, 필요에 따라 MySQL 커넥터 의존성을 주석 해제하여 사용하면 됩니다.

## 테스트

`src/test/java` 디렉터리의 통합 테스트에서는 애플리케이션 컨텍스트 로딩과 사용자 인증 관련 로직을 점검합니다. 환경 설정 파일이 없으면 테스트가 실패할 수 있습니다.
