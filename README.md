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
