# RESTful Resource Booking System

A RESTful Resource Booking System built with **Spring Boot**, **Spring Security**, **JWT authentication**, **JPA/Hibernate** and **MySQL**.

The system provides role-based access control so that administrators and users can manage resources and reservations.

> Final Project Assignment – this repository contains the complete project code and documentation.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [User Roles](#user-roles)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Reservation Status](#reservation-status)
- [Pagination, Sorting and Filtering](#pagination-sorting-and-filtering)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Swagger / OpenAPI Documentation](#swagger--openapi-documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Security](#security)
- [License](#license)

---

## Features

- JWT-based authentication
- Role-based authorization using `ADMIN` and `USER` roles
- User registration and login
- Resource management
- Reservation management
- Reservation status management
- User-specific reservation access
- Resource-specific reservation access
- Reservation filtering by:
  - Status
  - Minimum price
  - Maximum price
- Pagination and sorting
- Request validation
- Global exception handling
- Swagger/OpenAPI documentation
- Unit and controller tests
- Password hashing using BCrypt

---

## Tech Stack

| Category        | Technology                          |
|-----------------|-------------------------------------|
| Language        | Java 21                             |
| Framework       | Spring Boot 4.1.1                   |
| Web             | Spring Web MVC                      |
| Persistence     | Spring Data JPA, Hibernate          |
| Security        | Spring Security, JWT                |
| Database        | MySQL                               |
| Build tool      | Maven                               |
| Utilities       | Lombok                              |
| Testing         | JUnit 5, Mockito                    |
| API docs        | Springdoc OpenAPI / Swagger UI      |

---

## User Roles

### ADMIN

Administrators can:

- Manage users
- Create, update and delete resources
- View and manage reservations
- Update reservation status
- Access reservation data across all users and resources

### USER

Users can:

- View available resources
- Create reservations
- View their own reservations
- View reservation details
- Access only the data permitted to their role

---

## Authentication

The application uses JWT for authentication.

### Login

```http
POST /auth/login
```

Example request body:

```json
{
  "email": "user@gmail.com",
  "password": "Password@123"
}
```

A successful login returns a JWT access token. Send it in the `Authorization` header of every subsequent authenticated request:

```http
Authorization: Bearer <JWT_TOKEN>
```

---

## API Endpoints

### Authentication

| Method | Endpoint         | Access |
|--------|------------------|--------|
| POST   | `/auth/login`    | Public |
| POST   | `/auth/register` | Public |

### Users

| Method | Endpoint                 | Access       |
|--------|--------------------------|--------------|
| GET    | `/api/booking/users`     | ADMIN        |
| GET    | `/api/booking/users/{id}`| ADMIN / USER |
| PUT    | `/api/booking/users/{id}`| ADMIN / USER |
| DELETE | `/api/booking/users/{id}`| ADMIN        |

### Resources

| Method | Endpoint                      | Access       |
|--------|-------------------------------|--------------|
| GET    | `/api/booking/resources`      | ADMIN / USER |
| GET    | `/api/booking/resources/{id}` | ADMIN / USER |
| POST   | `/api/booking/resources`      | ADMIN        |
| PUT    | `/api/booking/resources/{id}` | ADMIN        |
| DELETE | `/api/booking/resources/{id}` | ADMIN        |

### Reservations

| Method | Endpoint                                                        | Access       |
|--------|-----------------------------------------------------------------|--------------|
| POST   | `/api/booking/reservations`                                     | ADMIN / USER |
| GET    | `/api/booking/reservations`                                     | ADMIN        |
| GET    | `/api/booking/reservations/{id}`                                | ADMIN / USER |
| PUT    | `/api/booking/reservations/{id}`                                | ADMIN        |
| DELETE | `/api/booking/reservations/{id}`                                | ADMIN / USER |
| GET    | `/api/booking/reservations/users/{id}`                          | ADMIN / USER |
| GET    | `/api/booking/reservations/resources/{id}`                      | ADMIN / USER |
| GET    | `/api/booking/reservations/users/{id}/status/{status}`          | ADMIN / USER |
| GET    | `/api/booking/reservations/resources/{id}/status/{status}`      | ADMIN / USER |

> **Note:** For endpoints marked `ADMIN / USER`, a `USER` can only access their own data (ownership checks are enforced). `ADMIN` can access everything.

---

## Reservation Status

Reservations support the following statuses:

| Status      | Meaning                                  |
|-------------|------------------------------------------|
| `PENDING`   | Reservation created, awaiting approval   |
| `CONFIRMED` | Reservation approved                     |
| `CANCELLED` | Reservation cancelled                    |

---

## Pagination, Sorting and Filtering

Reservation endpoints support pagination and sorting.

**Pagination and sorting:**

```http
GET /api/booking/reservations/users/7?page=0&size=3&sort=resource.price,desc
```

**Price filtering:**

```http
GET /api/booking/reservations/users/7?page=0&size=3&minPrice=150000&maxPrice=300000
```

**Status filtering:**

```http
GET /api/booking/reservations/users/7/status/CONFIRMED
```

Sorting follows the Spring Data format:

```text
sort=property,asc
sort=property,desc
```

Example: `sort=resource.price,desc`

---

## Configuration

### Database

The application uses MySQL. Create the database first:

```sql
CREATE DATABASE bookingSystemDB;
```

### Environment Variables

Database credentials and the JWT secret are supplied through environment variables.

| Variable      | Description                       |
|---------------|-----------------------------------|
| `DB_USERNAME` | MySQL username                    |
| `DB_PASSWORD` | MySQL password                    |
| `SECRET_KEY`  | Secret used to sign JWT tokens (256-bit) |

Example:

```bash
DB_USERNAME=root
DB_PASSWORD=your_database_password
SECRET_KEY=your_256_bit_secret_key
```

### Application Configuration

`src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bookingSystemDB
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${SECRET_KEY}
```

---

## Running the Application

### Prerequisites

Make sure the following are installed:

- Java 21
- Maven
- MySQL

### Steps

1. Clone the repository:

   ```bash
   git clone https://github.com/exelynt-learning-platform/backend-developer-as-final-83288-akash.git
   cd backend-developer-as-final-83288-akash
   ```

2. Set the required [environment variables](#environment-variables).

3. Create the MySQL database:

   ```sql
   CREATE DATABASE bookingSystemDB;
   ```

4. Run the application:

   ```bash
   mvn spring-boot:run
   ```

The application starts on <http://localhost:8080>.

---

## Swagger / OpenAPI Documentation

| Resource           | URL                                           |
|--------------------|-----------------------------------------------|
| Swagger UI         | http://localhost:8080/swagger-ui/index.html   |
| OpenAPI spec       | http://localhost:8080/v3/api-docs             |

Swagger provides interactive documentation for the available REST APIs.

For protected endpoints, click the **Authorize** button and provide:

```text
Bearer <JWT_TOKEN>
```

---

## Testing

The project includes unit and controller-level tests covering:

- Authentication
- Authorization
- User operations
- Resource operations
- Reservation operations
- Validation
- Exception handling
- Role-based access control
- Ownership checks

Run the complete test suite:

```bash
mvn test
```

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.bookingSystem
│   │       ├── config
│   │       ├── contorller
│   │       ├── dto
│   │       ├── entity
│   │       ├── exception
│   │       ├── helper
│   │       ├── repository
│   │       ├── security
│   │       └── service
│   └── resources
│       └── application.yml
└── test
    └── java
```

---

## Security

The application implements:

- JWT authentication
- Role-based authorization
- Password hashing using BCrypt
- Ownership checks for user-specific operations
- Protected API endpoints
- Secure handling of database credentials through environment variables

Passwords are never returned in API responses.

---

## License

This project was developed as a backend developer final project assignment.
