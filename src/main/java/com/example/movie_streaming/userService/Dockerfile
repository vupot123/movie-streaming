FROM eclipse-temurin:17-jdk
LABEL authors="VU"

WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
