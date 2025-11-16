# LocalLink

LocalLink is a microservices-based platform designed to facilitate local service bookings, reviews, payments, and more. The project is structured with a strong focus on backend architecture, leveraging Spring Boot, Eureka for service discovery, and a gateway for API management.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Backend Services](#backend-services)
- [API Gateway](#api-gateway)
- [Service Discovery (Eureka)](#service-discovery-eureka)
- [How to Run](#how-to-run)
- [Development](#development)
- [Folder Structure](#folder-structure)
- [Frontend](#frontend)
- [License](#license)

---

## Architecture Overview

LocalLink follows a microservices architecture. Each core domain is implemented as a separate Spring Boot service. Services communicate via REST APIs, and service discovery is managed by Eureka. The API Gateway routes requests to the appropriate backend service.

### Major Backend Components

- **ApiGateway**: Central entry point for all client requests. Handles routing, authentication, and cross-cutting concerns.
- **EurekaServer**: Service registry for dynamic discovery of backend services.
- **AvailabilityService**: Manages service availability and scheduling.
- **BookingService**: Handles booking logic and persistence.
- **PaymentService**: Manages payment processing and transactions.
- **ReviewService**: Handles user reviews and ratings.
- **ServiceListingService**: Manages service listings and provider information.
- **UserService**: Manages user accounts, authentication, and profiles.

## Backend Services

Each backend service is a standalone Spring Boot application with its own `pom.xml`, source code, and resources. Services are loosely coupled and communicate via HTTP REST APIs. Common patterns include:

- **Controller Layer**: Exposes REST endpoints.
- **Service Layer**: Business logic.
- **Repository Layer**: Data access (typically using Spring Data JPA).

### Example Service Structure

```
ServiceName/
├── src/main/java/... (Controllers, Services, Models)
├── src/main/resources/
├── pom.xml
```

## API Gateway

The `ApiGateway` uses Spring Cloud Gateway to route requests to backend services. It can be configured for authentication, rate limiting, and more. All client requests should go through the gateway.

## Service Discovery (Eureka)

The `EurekaServer` enables dynamic registration and discovery of services. Each backend service registers itself with Eureka on startup, allowing for load balancing and failover.

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+

### Steps

1. **Start Eureka Server**
   - Navigate to `EurekaServer` and run:
     ```sh
     ./mvnw spring-boot:run
     ```
2. **Start Backend Services**
   - For each service (e.g., `BookingService`, `PaymentService`, etc.), run:
     ```sh
     ./mvnw spring-boot:run
     ```
   - Ensure each service is configured to register with Eureka.
3. **Start API Gateway**
   - Navigate to `ApiGateway` and run:
     ```sh
     ./mvnw spring-boot:run
     ```
4. **Access the Application**
   - All API requests should be sent to the API Gateway endpoint.

## Development

- Each service can be developed and tested independently.
- Use the provided `mvnw` wrapper for Maven commands.
- Configuration files are located in `src/main/resources` of each service.
- Unit and integration tests are in `src/test/java`.

## Folder Structure

```
LocalLink/
├── ApiGateway/
├── AvailabilityService/
├── BookingService/
├── EurekaServer/
├── PaymentService/
├── ReviewService/
├── ServiceListingService/
├── UserService/
├── frontend/
```

## Frontend

The frontend is a separate Vite + React application located in the `frontend` folder. It communicates with the backend via the API Gateway.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
