# ========= STAGE 1: BUILD THE APP =========
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven files first (better caching)
COPY pom.xml .
COPY src ./src

# Build the Spring Boot JAR
RUN mvn clean package -DskipTests

# ========= STAGE 2: RUN THE APP =========
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy only the final JAR from the build stage
COPY --from=build /app/target/dentrohills-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]