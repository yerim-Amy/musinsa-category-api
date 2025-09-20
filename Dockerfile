FROM openjdk:11-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일을 컨테이너로 복사
COPY target/category-api-0.0.1-SNAPSHOT.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]