# EventSphere

A full-stack Event Management and Ticket Booking Platform built with **Spring Boot** and **React**.

EventSphere provides a complete platform for managing events, booking tickets, processing payments, managing users and roles, handling notifications, collecting reviews, and providing organizer analytics.

## 🚀 Features

### 🔐 Authentication & Authorization

* User registration and login
* JWT-based authentication
* Stateless Spring Security configuration
* Role-based access control
* Roles:

  * `ADMIN`
  * `ORGANIZER`
  * `USER`
* Organizer ownership validation
* Protected frontend routes

### 👤 User Management

* View and update personal profile
* Admin user management
* Role management for administrators
* Email uniqueness validation

### 🎫 Event Management

* Create and manage events
* Event categories
* Event search and filtering
* Pagination and sorting
* Event ownership for organizers
* Event cancellation
* Cancelled event handling across bookings and payments

### 🎟️ Booking System

* Book event tickets
* Capacity validation
* Automatic ticket price calculation
* Booking references
* Booking history
* Booking cancellation
* Admin booking management

### 💳 Payment Management

* Payment records linked to bookings
* Unique payment references
* Payment status tracking
* Duplicate payment prevention
* Refund handling for cancelled events

### ❤️ Favorites

* Add events to favorites
* Remove favorites
* View favorite events
* Check favorite status

### 🔔 Notifications

* User-specific notifications
* Booking notifications
* Event cancellation notifications
* Payment and refund notifications
* Unread notification count
* Mark notifications as read

### ⭐ Reviews

* Event reviews and ratings
* Review management
* User/event validation

### 📊 Organizer Dashboard

* Total events
* Upcoming events
* Completed events
* Total bookings
* Tickets sold
* Revenue
* Event-level insights
* Occupancy information
* Cancelled event visibility

### 🧑‍💼 Organizer Requests

* Users can request organizer access
* Admin approval/rejection
* Request status tracking
* Admin review information

### 📚 API Documentation

* RESTful API architecture
* Swagger/OpenAPI documentation
* Interactive API testing through Swagger UI

### 🧪 Testing

* Unit and integration testing
* Spring Boot testing
* Testcontainers
* JaCoCo code coverage
* **295 tests passing**

### ⚙️ DevOps & Production Preparation

* Environment-specific Spring profiles
* Dockerfile
* Docker Compose
* PostgreSQL container
* GitHub Actions CI
* Automated Maven testing and builds
* Spring Boot Actuator

---

## 🛠️ Tech Stack

### Backend

* Java 21
* Spring Boot 3.5.4
* Spring Security
* JWT / JJWT
* Spring Data JPA
* Hibernate
* PostgreSQL
* Maven
* Springdoc OpenAPI
* Spring Boot Actuator
* JUnit
* Mockito
* Testcontainers
* JaCoCo

### Frontend

* React
* Vite
* React Router
* Axios
* JavaScript
* HTML
* CSS

### DevOps & Tools

* Docker
* Docker Compose
* Git
* GitHub
* GitHub Actions

---

## 🏗️ Architecture

EventSphere follows a layered backend architecture built around Spring Boot.

```text
React Frontend
       │
       │ REST API
       ▼
Spring Boot Backend
       │
       ├── Controllers
       │
       ├── Services
       │
       ├── Repositories
       │
       ├── DTOs / Mappers
       │
       └── Security
              │
              ▼
          PostgreSQL
```

The frontend communicates with the backend through REST APIs, while Spring Security and JWT protect authenticated resources.

---

## 👥 User Roles

| Role      | Main Capabilities                                                                  |
| --------- | ---------------------------------------------------------------------------------- |
| USER      | Browse events, book tickets, manage favorites, reviews, notifications and bookings |
| ORGANIZER | Create and manage owned events, view analytics, manage event lifecycle             |
| ADMIN     | Manage users, roles, organizer requests and administrative booking information     |

Access to protected operations is enforced on the backend using Spring Security and method-level authorization.

---

## 🔄 Main Workflows

### User Registration & Login

```text
Register
   ↓
Login
   ↓
JWT Token
   ↓
Authenticated Requests
   ↓
Role-based Authorization
```

### Event Booking

```text
Browse Events
      ↓
Select Event
      ↓
Book Tickets
      ↓
Capacity Validation
      ↓
Payment
      ↓
Booking Confirmation
      ↓
Notification
```

### Event Cancellation

```text
Organizer Cancels Event
          ↓
Event Status → CANCELLED
          ↓
Existing Bookings Updated
          ↓
Successful Payments Refunded
          ↓
Users Notified
          ↓
Dashboard Metrics Updated
```

---

## 🔒 Security

EventSphere uses Spring Security with JWT-based authentication.

Security features include:

* Stateless authentication
* JWT token validation
* Protected REST endpoints
* Role-based authorization
* Organizer ownership checks
* Admin-only operations
* Authentication and authorization error handling
* Environment-based secret configuration

Sensitive configuration values such as database credentials and JWT secrets are provided through environment variables rather than being stored directly in source code.

---

## 📖 API Documentation

The backend exposes REST APIs for:

* Authentication
* Users
* Events
* Bookings
* Payments
* Favorites
* Notifications
* Reviews
* Dashboard analytics
* Organizer requests

Swagger/OpenAPI is configured for interactive API documentation and testing.

When running locally, Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## 🗄️ Database

EventSphere uses PostgreSQL with Spring Data JPA and Hibernate.

The database contains entities for major application domains including:

* Users
* Events
* Bookings
* Payments
* Favorites
* Notifications
* Reviews
* Organizer Requests

Database design documentation and diagrams are maintained separately in the `database/` and `diagrams/` directories.

---

## 🧪 Testing

The backend has an automated test suite covering controllers, services, repositories, security, dashboard functionality, event cancellation, bookings, payments and other application modules.

Latest full test result:

```text
Tests run: 295
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

JaCoCo is used for code coverage analysis.

Testcontainers is used for integration testing with PostgreSQL.

---

## 🐳 Docker

EventSphere includes Docker support for running the backend with PostgreSQL.

The project contains:

```text
Dockerfile
docker-compose.yml
```

The production-oriented configuration uses:

* Java 21 JRE
* PostgreSQL 16
* Environment variables for sensitive configuration
* Persistent PostgreSQL volume
* Database health checks
* Spring `prod` profile
* Hibernate schema validation

---

## 🔄 CI/CD

GitHub Actions is configured to automatically:

1. Check out the repository
2. Set up Java 21
3. Configure Maven caching
4. Run the backend test suite
5. Build the application

Current CI runs successfully on the `main` branch.

---

## 📁 Project Structure

```text
eventsphere/
│
├── .github/
│   └── workflows/
│
├── database/
│
├── diagrams/
│
├── docs/
│
├── eventsphere-backend/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── com/eventsphere/eventsphere_backend/
│   │   │           ├── auth/
│   │   │           ├── booking/
│   │   │           ├── common/
│   │   │           ├── config/
│   │   │           ├── dashboard/
│   │   │           ├── event/
│   │   │           ├── favorite/
│   │   │           ├── notification/
│   │   │           ├── organizer/
│   │   │           ├── payment/
│   │   │           ├── review/
│   │   │           └── user/
│   │   │
│   │   └── test/
│   │
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── pom.xml
│
├── eventsphere-frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   ├── routes/
│   │   └── services/
│   ├── package.json
│   └── vite.config.js
│
└── README.md
```

---

## ⚙️ Local Setup

### Prerequisites

Make sure the following are installed:

* Java 21
* Maven
* Node.js
* npm
* PostgreSQL
* Git

### Clone the Repository

```bash
git clone https://github.com/muditkumarrajput/eventsphere.git
cd eventsphere-main
```

### Backend

```bash
cd eventsphere-backend
```

Configure the required environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
SPRING_PROFILES_ACTIVE
```

Then start the backend:

```bash
./mvnw spring-boot:run
```

### Frontend

Open another terminal:

```bash
cd eventsphere-frontend
npm install
npm run dev
```

The Vite development server will provide the frontend URL in the terminal.

---

## 📌 Current Project Status

EventSphere's core full-stack functionality has been implemented and integrated.

Completed areas include:

* Authentication and authorization
* User management
* Event management
* Event filtering
* Booking
* Payments
* Favorites
* Notifications
* Reviews
* Organizer analytics
* Organizer requests
* Event cancellation and refunds
* Admin booking management
* Swagger/OpenAPI
* Testing
* Docker configuration
* GitHub Actions CI
* React frontend integration

The application has been tested locally, and the backend test suite currently passes all 295 tests.

Production deployment is planned separately.

---

## 🔮 Future Improvements

Potential future improvements include:

* Production deployment
* Real payment gateway integration
* Email delivery integration
* Additional frontend UX improvements
* Monitoring and observability enhancements
* Cloud infrastructure

---

## 👨‍💻 Author

**Mudit Kumar**

Java / Spring Boot Developer

GitHub:
https://github.com/muditkumarrajput

---

## 📄 License

This project is intended for learning, portfolio development, and demonstration purposes.

