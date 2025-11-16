#BUILD STAGE
FROM maven:3.9.8-eclipse-temurin-21 AS builder
LABEL authors="Sarthak"
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

#RUN STAGE
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]