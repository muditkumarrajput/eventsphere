# EventSphere Tech Stack

EventSphere is a full-stack event management and ticket booking platform built with a modern Java backend, React frontend, PostgreSQL database, and supporting DevOps tools.

## Backend

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot 3.5.4 | Backend application framework |
| Spring Web | REST API development |
| Spring Data JPA | Database persistence |
| Hibernate | ORM and database interaction |
| Spring Security | Authentication and authorization |
| JWT | Stateless authentication |
| Jakarta Bean Validation | Request and business validation |
| Maven | Dependency management and build automation |
| Spring Boot Actuator | Application monitoring and health endpoints |
| Springdoc OpenAPI | API documentation and Swagger UI |

## Frontend

| Technology | Purpose |
|---|---|
| React | Frontend user interface |
| Vite | Frontend build tool and development server |
| React Router | Client-side routing |
| Axios | HTTP requests to backend APIs |
| JavaScript | Frontend programming language |
| CSS | Application styling |

## Database

| Technology | Purpose |
|---|---|
| PostgreSQL 16 | Relational database |
| Hibernate | Object-relational mapping |
| Spring Data JPA | Repository and persistence layer |
| Testcontainers | Database integration testing |

## Security

EventSphere uses Spring Security with JWT-based authentication.

Main security components include:

- JWT authentication
- Stateless session management
- Role-based authorization
- Protected REST endpoints
- Organizer ownership validation
- Admin-only operations
- Custom authentication and access-denied handling

Supported roles:

- `USER`
- `ORGANIZER`
- `ADMIN`

## Testing

| Technology | Purpose |
|---|---|
| JUnit | Unit and integration testing |
| Spring Boot Test | Application testing |
| MockMvc | REST API testing |
| Testcontainers | PostgreSQL integration testing |
| JaCoCo | Test coverage reporting |

The backend test suite contains comprehensive tests covering authentication, users, events, bookings, payments, favorites, notifications, reviews, organizer requests, cancellation, refunds, dashboard functionality, and security.

## API Documentation

EventSphere provides API documentation using:

- OpenAPI
- Swagger UI
- Springdoc OpenAPI

This allows backend REST endpoints to be explored and tested through an interactive API interface.

## DevOps

| Technology | Purpose |
|---|---|
| Git | Version control |
| GitHub | Source code hosting |
| GitHub Actions | Continuous integration |
| Docker | Backend containerization |
| Docker Compose | Local multi-container environment |
| Maven Wrapper | Reproducible Maven builds |

## Configuration

EventSphere uses environment-based configuration for sensitive and environment-specific values.

Development configuration includes:

- PostgreSQL connection
- Database credentials
- JWT secret
- JWT expiration

Production configuration uses environment variables and validates the existing database schema rather than automatically modifying it.

## Project Architecture

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Additional layers include:

- DTOs
- Mappers
- Entities
- Security
- Exception handling
- Validation

The frontend communicates with the backend through REST APIs.

```text
React Frontend
      ↓
REST API
      ↓
Spring Boot Backend
      ↓
PostgreSQL
```

## Project Structure

```text
eventsphere-main/
│
├── eventsphere-backend/
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── eventsphere-frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.js
│
├── database/
│   └── README.md
│
├── diagrams/
│   └── README.md
│
├── README.md
└── TECH_STACK.md
```

## Development Environment

Recommended environment:

- Java 21
- Maven
- Node.js 22+
- npm
- PostgreSQL 16
- Docker
- Git

## Production Preparation

The project includes production-oriented configuration such as:

- Environment variables for secrets
- Separate development and production profiles
- Production database schema validation
- Docker containerization
- Docker Compose configuration
- GitHub Actions CI
- Spring Boot Actuator

Production deployment is planned separately.

## Summary

EventSphere combines:

**Java + Spring Boot + Spring Security + JWT + PostgreSQL + React + Vite + Docker + GitHub Actions**

to provide a complete full-stack event management and ticket booking platform.