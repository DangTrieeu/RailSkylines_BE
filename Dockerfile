# Build stage
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

# Sao chép toàn bộ mã nguồn
COPY . .

# Build dự án với Gradle, bỏ qua kiểm tra
RUN gradle clean build -x test

# Run stage
FROM openjdk:21-jdk-slim
WORKDIR /app

# Sao chép file JAR từ build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose cổng 8080
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]