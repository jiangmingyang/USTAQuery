# Stage 1: Build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Cache Maven dependencies
COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B

# Build application
COPY backend/src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xmx512m", "-Xms256m", \
  "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", \
  "-jar", "app.jar"]
