FROM eclipse-temurin:17-jdk
LABEL authors="VU"
WORKDIR /app
COPY target/*.jar app.jar
#COPY src/main/resources/molten-position-459001-i5-53f12afab112.json /app/service-account-file.json
#ENV GOOGLE_APPLICATION_CREDENTIALS=/app/service-account-file.json
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
