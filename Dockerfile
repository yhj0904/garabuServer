FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드된 JAR 파일 복사
ARG JAR_FILE=build/libs/garabuServer-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
