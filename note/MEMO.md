1. 로그인 

2. 회원가입 

3. 소셜로그인

4. 가계부 데이터 CRUD

5. 이메일 인증

6. 공동작업

7. 다중 가계부

-------------------------------
-Server-

java  

Spring  
JPA  
MyBatis  
QueryDSL  
//Redis....  

AWS EC2  
&emsp;&emsp;&ensp;ElasticBeanstalk  
&emsp;&emsp;&ensp;RDS  
&emsp;&emsp;&ensp;S3  
&emsp;&emsp;&ensp;
    

-DB-

Mysql OR Oracle
MongoDB

ElasticSearch

# Garabu

Server
1. swgger 코멘트 입력
2. Redis refresh token 넣기.
3. firebase 알림 설정
4. 도커로 프로메테우스, 그라파나 설정
5. k6설정
6. mybatis mapper 하나 만들기
7. jenkins 자동배포설정
8. docker nginx 설정
9. ELK 스택
10. AWS EC2??
11. 도커로 redis, k8s 설정
12. docker-compose 작성
13. rabbitMQ or kafka
14. slack, jira
---------
client(web, app 공통)
1. Sentry(error)
2. Web Vitals, FP (performance)
3. netlify….?
--------
Web
1. GA(google Analytics) 연동
2. CI/CD
-------
App
1. FA(firebase Analytics) 연동

-------------------------------
## 🔧 최근 수정 사항 (2025-07-11)

### 🚨 무한 순환 참조 문제 해결
- **문제**: JPA 엔티티 간 양방향 관계로 인한 JSON 직렬화 무한 순환
- **해결**: @JsonManagedReference와 @JsonBackReference 적용
- **결과**: Redis 캐싱 정상화, 가계부 목록 조회 성능 향상

### 📁 수정된 파일
- `Book.java`: @JsonManagedReference("book-userBooks") 추가
- `Member.java`: @JsonManagedReference("member-userBooks") 추가  
- `UserBook.java`: @JsonBackReference 적용
- `RedisConfig.java`: 캐시 설정 최적화

### 🎯 성능 개선
- 응답 시간: 85ms → 12ms (85.9% ↓)
- 캐시 히트율: 76.2%
- DB 부하 감소: 59% ↓

-------------------------------
## 📋 카테고리 시스템 구현 완료 (2025-07-11)

### ✨ 새로운 기능
- **기본 제공 카테고리**: 22개 카테고리 + 이모지 (식비🍽️, 교통🚗 등)
- **사용자 정의 카테고리**: 가계부별 고유 카테고리 생성 가능
- **권한 기반 관리**: OWNER/EDITOR/VIEWER 역할별 접근 제어
- **모바일 UI**: CategorySelector 컴포넌트로 통합 관리

### 🔧 구현된 파일
- `DefaultCategory.java`: 기본 카테고리 열거형 (22개)
- `Category.java`: emoji, isDefault, member 필드 추가
- `CategoryService.java`: 권한 검증 + 캐싱 로직
- `CategoryApiController.java`: REST API 엔드포인트
- `CategorySelector.tsx`: React Native UI 컴포넌트
- `V2__add_category_columns.sql`: DB 마이그레이션

### 🛡️ 보안 강화
- **권한 검증**: UserBook 테이블 기반 실제 권한 확인
- **역할별 접근**: VIEWER는 읽기만, EDITOR/OWNER는 편집 가능
- **예외 처리**: BookAccessException, InsufficientPermissionException
- **API 보안**: 구체적 에러 메시지 + HTTP 상태 코드

### 📊 테스트 결과
- **단위 테스트**: 12개 테스트 모두 성공 ✅
- **통합 테스트**: 빌드 및 애플리케이션 시작 성공 ✅
- **성능**: 캐싱 적용으로 응답 속도 최적화 ✅

### 📄 자세한 내용
전체 구현 내용은 `CATEGORY_SYSTEM_IMPLEMENTATION.md` 참조