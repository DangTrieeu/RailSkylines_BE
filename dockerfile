# Giai đoạn 1: Build ứng dụng
FROM openjdk:21-jdk-slim AS builder

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép các file cấu hình Gradle và mã nguồn
COPY build.gradle.kts settings.gradle.kts ./
COPY gradlew .
COPY gradle ./gradle
COPY src ./src

# Cấp quyền thực thi cho Gradle Wrapper
RUN chmod +x gradlew

# Xóa cache và build ứng dụng, bỏ qua test
RUN ./gradlew clean build -x test

# Giai đoạn 2: Tạo image chạy ứng dụng
FROM openjdk:21-jdk-slim

# Thiết lập thư mục làm việc
WORKDIR /app

# Sao chép file JAR chính từ giai đoạn build
COPY --from=builder /app/build/libs/railskylines-0.0.1-SNAPSHOT.jar app.jar

# Mở cổng 8080 cho ứng dụng Spring Boot
EXPOSE 8080

# Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]