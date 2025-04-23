FROM eclipse-temurin:17-jdk
LABEL authors="VU"
WORKDIR /app
COPY target/userService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
