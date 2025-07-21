# Garabu Server Dockerfile
# Multi-stage build for all environments (local, development, production)
# Build with: docker build -t garabuserver:latest .
# For specific stage: docker build --target=<stage> -t garabuserver:<tag> .

# ================== Build Stage ==================
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app

# Gradle build 캐시 최적화
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 프로덕션 빌드
ARG SKIP_TESTS=false
RUN if [ "$SKIP_TESTS" = "true" ]; then \
      gradle build -x test --no-daemon --parallel; \
    else \
      gradle build --no-daemon --parallel; \
    fi

# JAR 파일 확인 및 이름 변경
RUN ls -la build/libs/ && \
    mv build/libs/*-SNAPSHOT.jar app.jar || mv build/libs/*.jar app.jar

# ================== Local Development Stage ==================
FROM eclipse-temurin:21-jdk-alpine AS development
RUN apk add --no-cache curl tzdata

# 타임존 설정
ENV TZ=Asia/Seoul
RUN cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Gradle wrapper 복사
COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

# 의존성 파일 복사
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY src ./src

# 개발 환경 설정
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 포트 노출
EXPOSE 8080

# 개발 모드로 실행 (자동 재시작 지원)
CMD ["./gradlew", "bootRun", "--no-daemon"]

# ================== Production Runtime Stage ==================
FROM eclipse-temurin:21-jre-alpine AS production
RUN apk add --no-cache curl tzdata

# 타임존 설정
ENV TZ=Asia/Seoul
RUN cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# 로그 디렉토리 생성
RUN mkdir -p /app/logs /app/config /var/log/app /var/log/garabu

# Spring Boot 애플리케이션 JAR 복사
COPY --from=build /app/app.jar app.jar

# 비root 사용자 생성 및 권한 설정
RUN addgroup -g 1000 -S garabu && \
    adduser -u 1000 -S garabu -G garabu && \
    chown -R garabu:garabu /app && \
    chown -R garabu:garabu /var/log/app && \
    chown -R garabu:garabu /var/log/garabu && \
    chmod 755 /var/log/garabu

USER garabu

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 최적화 설정 (프로덕션)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+ExitOnOutOfMemoryError \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/heapdump.hprof \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ================== Default Stage (Production) ==================
FROM production