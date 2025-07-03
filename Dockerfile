FROM openjdk:21-jdk-slim

WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

# 빌드된 JAR 파일 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
