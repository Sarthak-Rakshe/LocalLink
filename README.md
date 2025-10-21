# LocalLink — Neighborhood Service Exchange

This repository contains a microservices-based platform where neighbors can offer and request local services (e.g., dog walking, lawn mowing, tutoring, babysitting, home repairs, grocery shopping).

## Modules in this repo

- AvailabilityService

  - Path: `AvailabilityService/`
  - Purpose: Manage available and booked time slots.
  - Tech: Spring Boot 3.5.x, Java 21, Actuator, Web, Validation, JPA, Eureka Client, OpenFeign
  - DB: PostgreSQL (runtime), H2 (dev/test)

- BookingService

  - Path: `BookingService/`
  - Purpose: Booking and scheduling between users and service listings.
  - Tech: Spring Boot 3.5.x, Java 21, Web, Validation, JPA, Mail; Eureka Client, OpenFeign
  - DB: PostgreSQL (runtime), H2 (tests)

- ServiceListingService

  - Path: `ServiceListingService/`
  - Purpose: Create and search service listings (offered services in a neighborhood).
  - Tech: Spring Boot 3.5.x, Java 21, Web, Validation, JPA
  - DB: PostgreSQL (runtime), H2 (tests)

- UserService

  - Path: `UserService/`
  - Purpose: User registration and basic profiles (providers and requestors).
  - Tech: Spring Boot 3.5.x, Java 21, Actuator, Web, Validation, JPA, Security (JWT libs included), Eureka Client, OpenFeign
  - DB: PostgreSQL (runtime), H2 (tests)

- PaymentService

  - Path: `PaymentService/`
  - Purpose: Payment orchestration (Razorpay integration planned).
  - Tech: Spring Boot 3.5.x, Java 21, Actuator, Web, Validation, JPA, Eureka Client, OpenFeign
  - DB: PostgreSQL (runtime), H2 (dev/test)

- ReviewService

  - Path: `ReviewService/`
  - Purpose: Simple reviews/ratings for services and providers.
  - Tech: Spring Boot 3.5.x, Java 21, Actuator, Web, Validation, JPA
  - DB: PostgreSQL (runtime), H2 (tests)

- EurekaServer
  - Path: `EurekaServer/`
  - Purpose: Service discovery registry.
  - Tech: Spring Cloud Netflix Eureka Server (Spring Cloud BOM 2025.0.0)

Note: Spring Cloud BOM 2025.0.0 is referenced across modules. Eureka/Feign are enabled in several services (AvailabilityService, BookingService, PaymentService, UserService); others may remain standalone for now.

## Quick start (local)

Prerequisites: Java 21. Each module includes the Maven Wrapper (`mvnw.cmd`). PostgreSQL is used at runtime where configured; tests use H2.

Build per module (Windows PowerShell):

```powershell
# From the repo root, run in each module folder you want to build
cd .\EurekaServer;          .\mvnw.cmd -q clean package; cd ..
cd .\UserService;           .\mvnw.cmd -q clean package; cd ..
cd .\ServiceListingService; .\mvnw.cmd -q clean package; cd ..
cd .\BookingService;        .\mvnw.cmd -q clean package; cd ..
cd .\AvailabilityService;   .\mvnw.cmd -q clean package; cd ..
cd .\PaymentService;        .\mvnw.cmd -q clean package; cd ..
cd .\ReviewService;         .\mvnw.cmd -q clean package; cd ..
```

Recommended run order (start Eureka first):

```powershell
# 1) Start Eureka (in a new terminal)
cd .\EurekaServer
.\mvnw.cmd spring-boot:run

# 2) Start services (each in its own terminal)
cd .\UserService;           .\mvnw.cmd spring-boot:run
cd .\ServiceListingService; .\mvnw.cmd spring-boot:run
cd .\BookingService;        .\mvnw.cmd spring-boot:run
cd .\AvailabilityService;   .\mvnw.cmd spring-boot:run
cd .\PaymentService;        .\mvnw.cmd spring-boot:run
cd .\ReviewService;         .\mvnw.cmd spring-boot:run
```

Configuration (ports, DB) is per service in `src/main/resources/application.properties`. If using PostgreSQL at runtime, set typical props:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/<db>`
- `spring.datasource.username=...`
- `spring.datasource.password=...`

## Current status

- UserService: create user and get-by-id endpoints working.
- ServiceListingService: core listing flow implemented (CRUD/search baseline).
- AvailabilityService: slot management foundation; integration tests present for apply/cancel booking flows.
- BookingService: domain scaffolding and REST endpoints under development; email dependency present.
- PaymentService: skeleton created; Razorpay integration pending.
- ReviewService: skeleton with test setup (H2, Mockito) in place.
- EurekaServer: available for service discovery; clients are wired in selected services.

## Domain and services (snapshot)

- UserService

  - Entities: User
  - Services: UserService, AuthService

- ServiceListingService

  - Entities: ServiceItem
  - Services: ServiceItemsService

- BookingService

  - Entities: Booking
  - Services: BookingService, CleanupService

- AvailabilityService

  - Entities: AvailabilityRules, ProviderExceptions
  - Services: AvailabilityService

- PaymentService

  - Entities: Transaction
  - Services: TransactionService, CleanupService, PayPalClient

- ReviewService
  - Entities: Review, ReviewAggregate
  - Services: ReviewService

## Roadmap (MVP)

- User registration and basic profiles
- Simple service listing and search
- Basic booking workflow + availability checks
- In-app messaging (basic)
- Simple review system

## Repository layout (high level)

- `AvailabilityService/`
- `BookingService/`
- `ServiceListingService/`
- `UserService/`
- `PaymentService/`
- `ReviewService/`
- `EurekaServer/`
- `README.md`, `LICENSE`

## Notes

- Use per-module `application.properties` to set ports and DB creds.
- Run tests per module: `mvnw.cmd test`.
- Spring Boot: 3.5.x; Spring Cloud BOM: 2025.0.0.
- Production concerns like gateway, messaging, and security hardening will be added incrementally.
