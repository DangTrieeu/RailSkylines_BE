# Students :

- **22110079 : Văn Công Toàn**
- **22110080 : Lý Đăng Triều**
- **22110083 : Phan Đình Trung**
- **22110086 : Trần Văn Tuyến**

# RailSkylines Backend

RailSkylines is an online train ticket booking system designed to provide a seamless experience for users to search, book, and manage train tickets. This repository contains the backend implementation built with **Java** and the **Spring Boot** framework.

## Table of Contents

- [Students :](#students-)
- [RailSkylines Backend](#railskylines-backend)
  - [Table of Contents](#table-of-contents)
  - [Features](#features)
  - [Technologies Used](#technologies-used)
  - [Project Structure](#project-structure)
  - [Prerequisites](#prerequisites)

## Features

- User authentication and authorization (login, registration, role-based access).
- Search for train schedules based on departure, destination, and date.
- Book and cancel train tickets.
- Manage user profiles and booking history.
- Payment integration (mock implementation, extendable to real gateways).
- Admin functionalities: manage trains, schedules, and bookings.

## Technologies Used

- **Java**: 21 (or specify your version)
- **Spring Boot**: 3.x (or your specific version)
- **Spring Data JPA**: For database interaction
- **Spring Security**: For authentication(OAuth2) and authorization
- **MySQL**: Database (configurable)
- **Gradle**: Dependency management
- **Lombok**: To reduce boilerplate code
- etc.

## Project Structure

```
RailSkylines/
├── src/
│   ├── main/
│   │   ├── java/com/railskylines/
│   │   │   ├── config/           # Configuration classes (e.g., SecurityConfig, JWTConfig)
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── domain/          # Data Transfer Objects
│   │   │   │   ├── request/     # Request DTOs
│   │   │   │   ├── response/    # Response DTOs
│   │   │   │   └── entity files      # JPA entities
│   │   │   ├── integration/     # Payment gateway
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── service/         # Business logic services
│   │   │   └── RailSkylinesApplication.java  # Main Spring Boot application class
│   │   └── resources/
│   │       ├── application.properties  # Application configuration
│   │       └── templates/       # Thymeleaf templates for email
│   └── test/
│       ├── java/com/railskylines/  # Unit and integration tests
│       └── resources/           # Test resources (if any)
├── .env                         # Environment variables for local development
├── .gitignore                   # Git ignore file
├── build.gradle.kts               # Gradle build configuration
├── Dockerfile                   # Docker configuration for building and running the app
├── README.md                    # Project documentation
└── gradle/                      # Gradle wrapper files
```

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 21+
- **Maven**: Version 3.6+
- **MySQL**: Database server (or use an in-memory DB like H2 for testing)
- **Git**: For version control
- IDE (optional): IntelliJ IDEA, Eclipse, or VS Code with Java extensions
