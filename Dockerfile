# JAVA 21
FROM openjdk:21-jdk-slim

# 작업 디렉토리 생성
WORKDIR /app

# 빌드된 JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "websocket-app.jar"]