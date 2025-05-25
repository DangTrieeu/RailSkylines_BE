# RailSkylines Backend

RailSkylines is an online train ticket booking system designed to provide a seamless experience for users to search, book, and manage train tickets. This repository contains the backend implementation built with **Java** and the **Spring Boot** framework.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [License](#license)

## Features
- User authentication and authorization (login, registration, role-based access).
- Search for train schedules based on departure, destination, and date.
- Book and cancel train tickets.
- Manage user profiles and booking history.
- Payment integration (mock implementation, extendable to real gateways).
- Admin functionalities: manage trains, schedules, and bookings.

## Technologies Used
- **Java**: java 21
- **Spring Boot**: 3.4.5
- **Spring Data JPA**: For database interaction
- **Spring Security**: For authentication and authorization
- **MySQL**: Database (configurable)
- **Gradle**: Dependency of project
- **Lombok**: To reduce boilerplate code

## Project Structure
```
RailSkylines/
├── src/
│   ├── main/
│   │   ├── java/com/railskylines/
│   │   │   ├── config/          # Configuration classes (e.g., SecurityConfig)
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── domain/          # JPA entities
│   │   │   │   ├──request/      # Request Data Transfer Object
|   |   |   |   └──response/     # Response Data Transfer Object
│   │   │   ├── integration/     # Payment Gateway
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   ├── service/         # Business logic services
│   │   │   ├── util/            # helper
│   │   │   └── RailSkylinesApplication.java  # Main application class
│   │   └── resources/
│   │       └── application.properties  # Configuration file
│   └── test/                  # Unit and integration tests
├── build.gradle.kts           # Dependencies and build configuration
└── README.md                  # This file
```

## Setup and Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/DangTrieeu/RailSkylines_BE.git
   cd RailSkylines_BE
   ```

2. **Configure the database**:
   - Create a schema `railskylines` in MySQL.
   - Update the `application.properties` file in `src/main/resources/` with your database credentials:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/railskylines
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     spring.jpa.hibernate.ddl-auto=update
     ```
## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.