# EventSphere Database

EventSphere uses **PostgreSQL** as its relational database and **Spring Data JPA/Hibernate** for persistence.

The database is organized around the major business domains of the application.

## Core Entities

### User

Stores registered users and their roles.

Main responsibilities:

* User identity
* Authentication information
* Contact information
* Role management
* Account timestamps

Roles:

* `USER`
* `ORGANIZER`
* `ADMIN`

---

### Event

Stores events created and managed by organizers.

Main responsibilities:

* Event title and description
* Location
* Event date
* Capacity
* Ticket price
* Category
* Event ownership
* Event status

Event statuses include:

* `ACTIVE`
* `CANCELLED`

---

### Booking

Represents a user's ticket reservation for an event.

Main responsibilities:

* Booking reference
* User association
* Event association
* Number of tickets
* Total amount
* Booking status
* Booking date

Bookings are associated with both users and events.

---

### Payment

Stores payment information associated with bookings.

Main responsibilities:

* Payment reference
* Booking association
* Payment amount
* Payment status
* Payment timestamps

Payment statuses include successful and refunded states.

---

### Favorite

Represents an event saved by a user.

A favorite connects:

```text
User ──── Favorite ──── Event
```

Duplicate favorites are prevented.

---

### Notification

Stores notifications generated for users.

Notifications can be generated for events such as:

* Booking confirmation
* Payment success
* Event cancellation
* Payment refund

Notifications also track whether they have been read.

---

### Review

Stores user reviews and ratings for events.

A review connects a user with an event and contains the user's rating and review information.

---

### Organizer Request

Stores requests from users who want organizer privileges.

Main responsibilities:

* Requesting user
* Request reason
* Request status
* Review information
* Admin reviewer
* Request timestamps

Request statuses include:

* `PENDING`
* `APPROVED`
* `REJECTED`

---

## Entity Relationships

The primary relationships can be summarized as:

```text
User
 │
 ├───────────────┐
 │               │
 ▼               ▼
Booking        Favorite
 │               │
 ▼               ▼
Event ◄──────────┘
 │
 └───────────────► Review

Booking
   │
   ▼
Payment

User
   │
   ▼
Notification

User
   │
   ▼
Organizer Request

User
   │
   └──────────────► Event
                     │
                     └── createdBy Organizer
```

## Database Design Principles

### Referential Integrity

Relationships between users, events, bookings, payments, favorites, reviews and notifications are represented through JPA entity relationships.

### Validation

Application-level validation is performed through Jakarta Bean Validation and service-layer business rules.

Examples include:

* Required fields
* Positive event capacity
* Valid ticket prices
* Future event dates
* Valid booking quantities
* Event ownership
* Role authorization

### Uniqueness

Important business identifiers use uniqueness constraints or application-level duplicate checks.

Examples:

* User email
* Booking reference
* Payment reference
* Favorite user/event combination

### Event Cancellation

Event cancellation is handled using an event status rather than deleting the event.

```text
ACTIVE
  │
  │ Organizer cancellation
  ▼
CANCELLED
```

This preserves historical event information while preventing new bookings.

### Payment Refunds

When a cancelled event has successful payments, the corresponding payment records can transition to a refunded state.

This allows payment history to remain associated with the original booking.

---

## Schema Management

Development configuration uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Production configuration uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This allows Hibernate to update the development schema while production validates the existing database schema instead of automatically modifying it.

---

## Database Technology

| Component           | Technology              |
| ------------------- | ----------------------- |
| Database            | PostgreSQL 16           |
| ORM                 | Hibernate               |
| Persistence         | Spring Data JPA         |
| Validation          | Jakarta Bean Validation |
| Integration Testing | Testcontainers          |

---

## Future Database Documentation

The `database/` directory can later contain:

* ER diagram
* Table relationship diagram
* Database migration documentation
* Sample development data documentation

The database design should remain synchronized with the entity model in:

```text
eventsphere-backend/src/main/java/
com/eventsphere/eventsphere_backend/
```
