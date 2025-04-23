
FROM eclipse-temurin:17-jdk

LABEL authors="VU"

WORKDIR /app

COPY target/actorService-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng ứng dụng
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
