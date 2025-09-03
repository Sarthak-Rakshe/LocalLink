# LocalLink — Neighborhood Service Exchange

This repository contains a microservices-based platform where neighbors can offer and request local services (e.g., dog walking, lawn mowing, tutoring, babysitting, home repairs, grocery shopping).

## Modules in this repo

- AvailabilityService

  - Path: `AvailabilityService/`
  - Purpose: Manage available and booked time slots.
  - Tech: Spring Boot 3.5.5, Java 21, Actuator, Web, Validation, JPA, Eureka Client, OpenFeign
  - DB: PostgreSQL (runtime), H2 (dev/test)

- BookingService

  - Path: `BookingService/BookingService/`
  - Purpose: Booking and scheduling between users and service listings.
  - Tech: Spring Boot 3.5.5, Java 21, Web, Validation, JPA
  - DB: PostgreSQL (runtime), H2 (tests)

- ServiceListingService

  - Path: `ServiceListingService/ServiceListingService/`
  - Purpose: Create and search service listings (offered services in a neighborhood).
  - Tech: Spring Boot 3.5.5, Java 21, Web, Validation, JPA
  - DB: PostgreSQL (runtime), H2 (tests)

- UserService
  - Path: `UserService/`
  - Purpose: User registration and basic profiles (providers and requestors).
  - Tech: Spring Boot 3.5.5, Java 21, Actuator, Web, Validation, JPA
  - DB: PostgreSQL (runtime), H2 (tests)

Note: Spring Cloud BOM 2025.0.0 is referenced across modules; some Cloud dependencies (Eureka/Feign) are enabled only in AvailabilityService currently.

## Quick start (local)

Prerequisites: Java 21. Maven Wrapper is included in each module.

Build all modules (run at repo root or per module):

1. Open PowerShell in the module folder (e.g., `AvailabilityService`, `BookingService/BookingService`, etc.).
2. Build: `./mvnw.cmd -q clean package`
3. Run: `./mvnw.cmd spring-boot:run`

Configuration (ports, DB) is controlled per service in `src/main/resources/application.properties`. PostgreSQL is used at runtime where configured; tests use H2.

## Current status

- UserService: create user and get by id endpoints implemented.
- ServiceListingService: core listing flow implemented.
- BookingService and AvailabilityService: scaffolding present; APIs under development.

## Roadmap (MVP)

- User registration and basic profiles
- Simple service listing and search
- Basic booking workflow + availability checks
- In-app messaging (basic)
- Simple review system

## Repository layout (high level)

- `AvailabilityService/`
- `BookingService/BookingService/`
- `ServiceListingService/ServiceListingService/`
- `UserService/`
- `README.md`, `LICENSE`

## Notes

- Use per-module `application.properties` to set ports and DB creds.
- Tests: `./mvnw.cmd test` in each module.
- Production concerns like gateway, discovery, and messaging will be added incrementally.
