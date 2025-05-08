# Stage 1: Build ứng dụng
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Sao chép file Gradle và cấu hình
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Sao chép mã nguồn
COPY src src

# Cấp quyền thực thi cho gradlew
RUN chmod +x gradlew

# Build ứng dụng, bỏ qua test để tăng tốc
RUN ./gradlew build -x test

# Stage 2: Tạo image chạy ứng dụng
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Sao chép file JAR từ stage build
COPY --from=builder /app/build/libs/*.jar app.jar

# Mở cổng mặc định của Spring Boot
EXPOSE 8080

# Thiết lập biến môi trường (có thể override khi chạy container)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

# Lệnh chạy ứng dụng
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]