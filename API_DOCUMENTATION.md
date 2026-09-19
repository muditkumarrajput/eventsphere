# EventSphere API Documentation

EventSphere exposes REST APIs through the Spring Boot backend.

The API uses JWT-based authentication and role-based authorization for protected operations.

## Base URL

```text
http://localhost:8080
```

## Authentication

Authentication is handled using JSON Web Tokens (JWT).

After successful login, the API returns a JWT token.

The token should be included in protected requests using:

```http
Authorization: Bearer <JWT_TOKEN>
```

## Authentication APIs

### Register

```http
POST /auth/register
```

Registers a new user.

### Login

```http
POST /auth/login
```

Authenticates a user and returns a JWT token.

## User APIs

### Get Current User

```http
GET /api/users/me
```

Returns the currently authenticated user's information.

### Update Current User

```http
PUT /api/users/me
```

Updates the authenticated user's profile.

### Get All Users

```http
GET /api/users
```

Admin-only endpoint for retrieving users.

### Change User Role

```http
PATCH /api/users/{id}/role
```

Admin-only endpoint for changing a user's role.

## Event APIs

### Create Event

```http
POST /api/events
```

Creates a new event.

Organizer authorization is required.

### Get Events

```http
GET /api/events
```

Returns available events.

Supports filtering, pagination, and sorting.

### Get Event

```http
GET /api/events/{id}
```

Returns details of a specific event.

### Update Event

```http
PUT /api/events/{id}
```

Updates an event.

Organizers can update their own events.

### Delete Event

```http
DELETE /api/events/{id}
```

Deletes an event according to the application's authorization rules.

### Cancel Event

```http
PATCH /api/events/{id}/cancel
```

Cancels an event created by the authenticated organizer.

Event cancellation also handles related bookings, refunds, notifications, and dashboard metrics.

## Booking APIs

### Create Booking

```http
POST /api/bookings
```

Creates a booking for an event.

The system validates:

- Event availability
- Event status
- Ticket quantity
- Available capacity
- User authentication

### Get My Bookings

```http
GET /api/bookings
```

Returns bookings belonging to the authenticated user.

### Get Booking

```http
GET /api/bookings/{id}
```

Returns details of an authenticated user's booking.

### Cancel Booking

```http
PATCH /api/bookings/{id}/cancel
```

Cancels an existing booking according to the application's business rules.

## Admin Booking APIs

### Get All Bookings

```http
GET /api/bookings/admin
```

Admin-only endpoint for retrieving bookings.

### Get Booking Details

```http
GET /api/bookings/admin/{id}
```

Admin-only endpoint for retrieving any booking by ID.

## Payment APIs

Payment APIs manage payment records associated with bookings.

Payment information includes:

- Payment reference
- Booking
- Amount
- Payment status
- Payment timestamps

Payment states include successful and refunded payments.

## Favorite APIs

### Add Favorite

```http
POST /api/favorites/{eventId}
```

Adds an event to the authenticated user's favorites.

### Get Favorites

```http
GET /api/favorites
```

Returns the user's favorite events.

### Check Favorite

```http
GET /api/favorites/{eventId}
```

Checks whether an event is in the user's favorites.

### Remove Favorite

```http
DELETE /api/favorites/{eventId}
```

Removes an event from the user's favorites.

## Notification APIs

### Get Notifications

```http
GET /api/notifications
```

Returns notifications for the authenticated user.

### Get Unread Count

```http
GET /api/notifications/unread-count
```

Returns the number of unread notifications.

### Mark Notification as Read

```http
PATCH /api/notifications/{id}/read
```

Marks a notification as read.

Notifications can be generated for:

- Booking confirmation
- Payment success
- Event cancellation
- Payment refund

## Review APIs

Review APIs allow users to provide feedback and ratings for events.

Reviews are associated with both the authenticated user and the event.

## Dashboard APIs

### Organizer Dashboard

```http
GET /api/dashboard
```

Returns organizer dashboard statistics.

Metrics include:

- Total events
- Upcoming events
- Completed events
- Total bookings
- Tickets sold
- Revenue

### Event Insights

```http
GET /api/dashboard/events
```

Returns event-level analytics such as:

- Tickets sold
- Remaining seats
- Occupancy percentage
- Revenue
- Event status

## Organizer Request APIs

Users can request organizer privileges.

The request workflow is:

```text
USER
  │
  ▼
Submit Organizer Request
  │
  ▼
PENDING
  │
  ├──────────────► APPROVED
  │
  └──────────────► REJECTED
```

Admin users review and process organizer requests.

## Authorization

EventSphere uses role-based access control.

| Role | Main Access |
|---|---|
| USER | Bookings, favorites, reviews, notifications |
| ORGANIZER | Event management, event analytics, cancellation |
| ADMIN | User management, role management, organizer requests, admin booking access |

Ownership rules are also applied to organizer-managed events.

## Error Handling

The API uses centralized exception handling.

Common HTTP responses include:

| Status | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created successfully |
| `400 Bad Request` | Invalid request or business validation failure |
| `401 Unauthorized` | Authentication required or invalid |
| `403 Forbidden` | Insufficient permissions |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Business conflict or duplicate operation |

## Swagger / OpenAPI

EventSphere provides interactive API documentation using Springdoc OpenAPI and Swagger UI.

When the backend is running locally, Swagger UI can be accessed through the configured Springdoc endpoint.

The Swagger interface allows developers to:

- Explore API endpoints
- View request and response models
- Review authorization requirements
- Test endpoints interactively

## API Architecture

```text
React Frontend
       │
       │ HTTP / REST
       ▼
Spring Boot Controllers
       │
       ▼
Service Layer
       │
       ▼
Repository Layer
       │
       ▼
PostgreSQL
```

## Related Documentation

- Main project documentation: `README.md`
- Technology stack: `TECH_STACK.md`
- Database documentation: `database/README.md`
- Architecture diagrams: `diagrams/README.md`