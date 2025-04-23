FROM eclipse-temurin:17-jdk
LABEL authors="VU"
WORKDIR /app
COPY target/seriesService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
