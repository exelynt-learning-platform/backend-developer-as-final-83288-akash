# backend-developer-as-final-83288-akash
Final Project Assignment - This repository contains the complete final project code and documentation.

# RESTful Resource Booking System

A RESTful Resource Booking System built using Spring Boot, Spring Security, JWT authentication, JPA/Hibernate, and MySQL.

The system provides role-based access control for administrators and users to manage resources and reservations.

---

## Features

- JWT-based authentication
- Role-based authorization using ADMIN and USER roles
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
- Pagination
- Sorting
- Request validation
- Global exception handling
- Swagger/OpenAPI documentation
- Unit and controller tests
- Password hashing using BCrypt

---

## Tech Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- MySQL
- Maven
- Lombok
- JUnit 5
- Mockito
- Springdoc OpenAPI / Swagger UI

---

## User Roles

### ADMIN

Administrators can:

- Manage users
- Create, update and delete resources
- View and manage reservations
- Update reservation status
- Access reservation data across users and resources

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

Example request:
{
  "email": "user@gmail.com",
  "password": "Password@123"
}

A successful login returns a JWT access token.
Use the token in subsequent authenticated requests:
Authorization: Bearer <JWT_TOKEN>


Reservation Status
Reservations support the following statuses:
- PENDING
- CONFIRMED
- CANCELLED

Pagination, Sorting and Filtering
Reservation endpoints support pagination and sorting.
Example:
GET /api/booking/reservations/users/7?page=0&size=3&sort=resource.price,desc

Price filtering is also supported:
GET /api/booking/reservations/users/7?page=0&size=3&minPrice=150000&maxPrice=300000

Sorting follows Spring Data format:
sort=property,asc
sort=property,desc

Example:
sort=resource.price,desc


Database Configuration
The application uses MySQL.
Create the database:
CREATE DATABASE bookingSystemDB;

Database credentials are supplied through environment variables.

Environment Variables
DB_USERNAME
DB_PASSWORD
SECRET_KEY

Example:
DB_USERNAME=root
DB_PASSWORD=your_database_password
SECRET_KEY=your_256_bit_secret_key

Application Configuration
The application reads database credentials and the JWT secret from environment variables.
Example configuration:
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bookingSystemDB
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${SECRET_KEY}

  Running the Application
Prerequisites
Make sure the following are installed:
- Java 17 or higher
- Maven
- MySQL
Steps
Clone the repository:

git clone <https://github.com/exelynt-learning-platform/backend-developer-as-final-83288-akash>

Configure the required environment variables.
Create the MySQL database:
CREATE DATABASE bookingSystemDB;

Run the application:
mvn spring-boot:run

The application starts on:
http://localhost:8080

Swagger / OpenAPI Documentation
Swagger UI is available at:
http://localhost:8080/swagger-ui/index.html

OpenAPI specification:
http://localhost:8080/v3/api-docs

Swagger provides interactive documentation for the available REST APIs.
For protected endpoints, use the Authorize button and provide:
Bearer <JWT_TOKEN>

Testing
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

Run the complete test suite using:
mvn test

Project Structure
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
│   │
│   └── resources
│       └── application.yml
│
└── test
    └── java

    Security
The application implements:
- JWT authentication
- Role-based authorization
- Password hashing using BCrypt
- Ownership checks for user-specific operations
- Protected API endpoints
- Secure handling of database credentials through environment variables
Passwords are never returned in API responses.

License
This project was developed as a backend developer final project assignment.
