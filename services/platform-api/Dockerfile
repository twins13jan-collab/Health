FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/platform-api-*.jar app.jar
USER 1001
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
