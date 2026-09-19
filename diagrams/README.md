# EventSphere Diagrams

This directory contains architecture and design diagrams for the EventSphere platform.

## System Architecture

~~~text
                         ┌─────────────────────┐
                         │    React Frontend   │
                         │      (Vite)         │
                         └──────────┬──────────┘
                                    │
                              REST API / HTTP
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Spring Boot API   │
                         │                     │
                         │  Controllers        │
                         │  Services           │
                         │  Repositories       │
                         │  DTOs / Mappers     │
                         │  Security / JWT     │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     PostgreSQL      │
                         │      Database       │
                         └─────────────────────┘
~~~

## Authentication Flow

~~~text
User
 │
 ▼
Login / Register
 │
 ▼
AuthController
 │
 ▼
AuthService
 │
 ▼
JWT Token
 │
 ▼
Frontend
 │
 ▼
Protected API Request
 │
 ▼
JwtAuthenticationFilter
 │
 ▼
Spring Security
 │
 ▼
Authorized Endpoint
~~~

## Event Booking Flow

~~~text
User
 │
 ▼
Browse Events
 │
 ▼
Select Event
 │
 ▼
Create Booking
 │
 ▼
Validate User & Event
 │
 ▼
Validate Capacity
 │
 ▼
Calculate Total Amount
 │
 ▼
Create Booking
 │
 ▼
Payment
 │
 ▼
Booking Confirmation
 │
 ▼
Notification
~~~

## Event Cancellation Flow

~~~text
Organizer
 │
 ▼
Cancel Event
 │
 ▼
Event Status → CANCELLED
 │
 ├──────────────► Cancel Existing Bookings
 │
 ├──────────────► Refund Successful Payments
 │
 ├──────────────► Create Notifications
 │
 └──────────────► Update Dashboard Metrics
~~~

## Role-Based Access

~~~text
                    ┌──────────────┐
                    │     User     │
                    └──────┬───────┘
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
          USER         ORGANIZER        ADMIN
             │             │             │
             ▼             ▼             ▼
        Bookings       Own Events    User Management
        Favorites      Analytics     Role Management
        Reviews        Cancellation  Organizer Requests
        Notifications  Event Mgmt    Admin Bookings
~~~

## Related Documentation

- Database design: `../database/README.md`
- Main project documentation: `../README.md`