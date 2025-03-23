# Qualifaize-Backend-API

**QualifAIze** is an AI-powered recruitment and interview management system designed to qualify, assess, and optimize hiring decisions through automation and intelligence.

## Tech Stack

This project is developed with the following technologies:

- **Java 21** – Modern Java for high performance and maintainability.
- **Spring Boot 3.4.4** – Backend framework for building robust microservices.
- **PostgreSQL** – Relational database management system for data storage.
- **Flyway** – Database migrations for version control and schema consistency.
- **Spring Security** – Authentication and authorization for user management.
- **Spring Boot Default Logger** – Used for logging application events.
- **Swagger API Documentation** – Provides interactive API documentation for easier development and testing.
- **MapStruct** – Used for efficient object mapping between DTOs and entities.
- **Gradle** – Build automation tool used for dependency management and project compilation.

## Architecture

- **RESTful APIs** – Services communicate using REST-based APIs, providing interoperability with frontend clients and other services.
- **Database Management** – PostgreSQL is used for structured data storage, with Flyway handling schema migrations efficiently.
- **Security** – Spring Security ensures secure access control and user authentication.
- **Logging** – The application utilizes Spring Boot’s default logging mechanism for tracking system events.
- **Object Mapping** – MapStruct is used to streamline the conversion of data between different layers of the application.

## Setup Instructions

To set up and run the project locally:

1. **Clone the repository**:
   ```sh
   git clone https://github.com/your-repo/qualifaize-backend-api.git
   cd qualifaize-backend-api
   ```

2. **Configure the database**:
    - Ensure you have PostgreSQL installed and running.
    - Update `application.yml` with your database credentials.

3. **Run Flyway migrations**:
   ```sh
   ./gradlew flywayMigrate
   ```

4. **Build and run the application**:
   ```sh
   ./gradlew clean build
   ./gradlew bootRun
   ```

5. **API Access**:
    - The application exposes RESTful APIs on `http://localhost:8080`
    - Swagger API Documentation is available at `http://localhost:8080/swagger-ui.html`