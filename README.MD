# RailSkylines Backend

RailSkylines is an online train ticket booking system designed to provide a seamless experience for users to search, book, and manage train tickets. This repository contains the backend implementation built with **Java** and the **Spring Boot** framework.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup and Installation](#setup-and-installation)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Contributing](#contributing)
- [License](#license)

## Features
- User authentication and authorization (login, registration, role-based access).
- Search for train schedules based on departure, destination, and date.
- Book and cancel train tickets.
- Manage user profiles and booking history.
- Payment integration (mock implementation, extendable to real gateways).
- Admin functionalities: manage trains, schedules, and bookings.

## Technologies Used
- **Java**: 17 (or specify your version)
- **Spring Boot**: 3.x (or your specific version)
- **Spring Data JPA**: For database interaction
- **Spring Security**: For authentication and authorization
- **MySQL/PostgreSQL**: Database (configurable)
- **Maven**: Dependency management
- **Lombok**: To reduce boilerplate code
- **Swagger**: API documentation (optional, if implemented)

## Project Structure
```
RailSkylines/
├── src/
│   ├── main/
│   │   ├── java/com/railskylines/
│   │   │   ├── config/           # Configuration classes (e.g., SecurityConfig)
│   │   │   ├── controller/       # REST API controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   ├── service/         # Business logic services
│   │   │   └── RailSkylinesApplication.java  # Main application class
│   │   └── resources/
│   │       ├── application.properties  # Configuration file
│   │       └── static/         # Static resources (if any)
│   └── test/                  # Unit and integration tests
├── pom.xml                    # Maven dependencies and build configuration
└── README.md                  # This file
```

## Prerequisites
Before you begin, ensure you have the following installed:
- **Java Development Kit (JDK)**: Version 17+
- **Maven**: Version 3.6+
- **MySQL/PostgreSQL**: Database server (or use an in-memory DB like H2 for testing)
- **Git**: For version control
- **OpenAI API Key**: Required for the AI chatbot and article embedding features
- IDE (optional): IntelliJ IDEA, Eclipse, or VS Code with Java extensions

## Setup and Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/railskylines-backend.git
   cd railskylines-backend
   ```

2. **Configure the database**:
   - Create a database (e.g., `railskylines_db`) in MySQL/PostgreSQL.
   - Update the `application.properties` file in `src/main/resources/` with your database credentials:
     ```properties
     spring.datasource.url=jdbc:mysql://localhost:3306/railskylines_db
     spring.datasource.username=your_username
     spring.datasource.password=your_password
     spring.jpa.hibernate.ddl-auto=update
     ```

3. **Configure the OpenAI API key for chatbot**:
   - Create a `.env` file in the project root (it's already in `.gitignore`):
     ```bash
     OPENAI_API_KEY=sk-your-actual-openai-api-key-here
     ```
   - Or set it as an environment variable before running:
     ```bash
     # Linux/Mac/Git Bash
     export OPENAI_API_KEY="sk-your-actual-openai-api-key-here"
     
     # Windows PowerShell
     $env:OPENAI_API_KEY = "sk-your-actual-openai-api-key-here"
     
     # Windows CMD
     set OPENAI_API_KEY=sk-your-actual-openai-api-key-here
     ```
   - The application uses this key for:
     - Generating article embeddings (for semantic search in the chatbot)
     - Streaming AI chat responses via OpenAI's GPT models

4. **Install dependencies**:
   Run the following command to download all required Maven dependencies:
   ```bash
   ./gradlew clean build
   ```

## Running the Application
1. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```
   The application will start on `http://localhost:8080` by default.

2. **Verify chatbot embedding generation**:
   - On startup, the application automatically generates embeddings for all articles without embeddings.
   - Check the logs for messages like:
     ```
     Rebuilt embeddings for X article(s).
     ```
   - If you see warnings about "OpenAI API key missing", ensure the `OPENAI_API_KEY` environment variable is set.

2. **Access Swagger UI** (if enabled):
   Open your browser and navigate to:
   ```
   http://localhost:8080/swagger-ui.html
   ```

## API Endpoints
Here are some example endpoints (adjust based on your implementation):
- **Authentication**:
  - `POST /api/auth/register` - Register a new user
  - `POST /api/auth/login` - User login
- **Train Schedules**:
  - `GET /api/trains/schedules` - Search train schedules
- **Booking**:
  - `POST /api/bookings` - Book a ticket
  - `GET /api/bookings/history` - View booking history
- **Admin**:
  - `POST /api/admin/trains` - Add a new train (admin only)

(Refer to the Swagger documentation or update this section with your specific endpoints.)

## Contributing
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a Pull Request.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.