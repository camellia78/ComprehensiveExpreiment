# ========================================
# Stage 1: Build with Maven
# ========================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY dorm-server/pom.xml .
RUN mvn dependency:go-offline -B || true
COPY dorm-server/src ./src
RUN mvn package -DskipTests -q

# ========================================
# Stage 2: Run with JRE
# ========================================
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
