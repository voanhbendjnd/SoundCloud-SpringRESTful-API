# Stage 1: Build stage
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy các file cấu hình gradle trước để cache dependencies
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Download dependencies (giúp build nhanh hơn ở các lần sau)
RUN ./gradlew dependencies --no-daemon

# Copy source code và build file jar
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy file jar từ stage build sang
# Gradle mặc định xuất file jar vào build/libs/
COPY --from=build /app/build/libs/*.jar app.jar

# Khai báo port của Spring Boot
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]