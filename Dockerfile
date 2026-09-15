FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY artifacts/api-server/spring-app/pom.xml ./pom.xml
RUN mvn -B -q dependency:go-offline
COPY artifacts/api-server/spring-app/src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/support-platform-api-*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
