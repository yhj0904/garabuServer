FROM openjdk:21-jdk-slim

WORKDIR /app

# 필수 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

RUN addgroup --system app && adduser --system --ingroup app app

# JAR 파일 복사 (여러 위치에서 시도)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar 2>/dev/null || true
COPY garabuserver*.jar app.jar 2>/dev/null || true
COPY *.jar app.jar 2>/dev/null || true

# 로그 디렉토리 생성
RUN mkdir -p /app/logs && chown app:app /app/logs

EXPOSE 8080

# JVM 옵션 환경변수
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

USER app

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
