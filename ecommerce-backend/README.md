# E-Commerce Backend

A production-ready-designed e-commerce backend built with **Java 17, Spring Boot, Spring Security, JWT, MySQL, Redis, and Docker.** The project focuses on secure authentication, product and category management, concurrency-aware inventory reservation, order lifecycle management, mock payment processing, Redis caching, and API rate limiting.


## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture and Project Structure](#architecture-and-project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
    - [1. Clone the Repository](#1-clone-the-repository)
    - [2. Configure Environment Variables](#2-configure-environment-variables)
    - [3. Run with Docker Compose](#3-run-with-docker-compose)
    - [4. Run Locally from IntelliJ IDEA](#4-run-locally-from-intellij-idea)
- [API Documentation](#api-documentation)
- [Mock Payment Gateway](#mock-payment-gateway)
- [Security and Configuration Notes](#security-and-configuration-notes)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Future Improvements](#future-improvements)

## Overview

This project implements the core backend services required for an e-commerce application, following production-oriented design principles such as layered architecture, secure authentication, transaction management, centralized exception handling, and concurrency-aware business workflows.

Special attention has been given to concurrency and race-condition handling in inventory and payment workflows. Database locking and transactional operations help protect inventory updates and reservations during concurrent requests. Payment workflows are designed to handle retries and repeated operations carefully, while keeping order and payment states consistent.

The project is built for realistic backend development and workflow testing. Its payment integration uses a mock gateway rather than a real payment provider, and production deployment readiness still depends on environment-specific security configuration, integration testing, monitoring, and operational validation.


The application supports the main shopping lifecycle:

**User authentication → Product discovery → Cart management → Order creation → Inventory reservation → Payment processing**


## Features

### Authentication and User Management
- User registration and user management.
- Password-based authentication.
- JWT access tokens and refresh tokens.
- Refresh-token rotation and replay protection.
- Logout and refresh-token revocation.
- Role-based authorization.
- User soft deletion and restoration.

### Product and Category Management
- Create, retrieve, update, and manage products and categories.
- Soft deletion and restoration.
- Prevent category deletion when it is still in use.
- Redis caching for product and category read operations.
- Cache eviction when relevant data changes.

### Inventory Management
- Inventory creation and updates.
- Direct inventory updates and a separate add-stock operation.
- Inventory reservation during order creation.
- Inventory release when a pending order is canceled.
- Concurrency-aware inventory operations using database locking and transactional boundaries to reduce race conditions during simultaneous stock updates and reservations.

### Cart and Order Management
- Add eligible products to a cart.
- Only active products can be added to the cart.
- Stock validation and reservation are handled by the order workflow rather than cart addition.
- Create orders with order items.
- Reserve inventory when an order is created.
- Cancel eligible pending orders and release their reserved inventory.

### Payment Management
- Mock payment gateway with deterministic success and failure outcomes.
- Payment retry using the existing payment record.
- Payment and order status management.
- Refund workflow with inventory release after a successful mock refund.
- Failed payments do not automatically cancel orders, allowing a customer to retry.
- Careful handling of repeated payment operations, retries, and refunds to maintain consistent payment and order state transitions.


### Performance and API Protection
- Redis-based product and category caching.
- Redis-based login rate limiting.
- Configurable rate-limit thresholds and time windows.
- Centralized exception handling and application logging.
- API documentation through Swagger UI.

## Technology Stack

| Technology                  | Purpose                                          |
|-----------------------------|--------------------------------------------------|
| Java 17                     | Application runtime and language target          |
| Spring Boot                 | Application framework                            |
| Spring Web                  | REST APIs                                        |
| Spring Security             | Authentication and authorization                 |
| JWT                         | Access and refresh-token authentication          |
| Spring Data JPA / Hibernate | Persistence and ORM                              |
| MySQL 8                     | Relational database                              |
| Redis                       | Caching and rate limiting                        |
| Maven                       | Build and dependency management                  |
| Bean Validation             | Request validation                               |
| Lombok                      | Boilerplate reduction                            |
| Swagger / OpenAPI           | API documentation and interactive testing        |
| Docker                      | Containerized application runtime                |
| Docker Compose              | Local orchestration of the app, MySQL, and Redis |

## Architecture and Project Structure

The application follows a layered architecture, separating API handling, business logic, persistence, and cross-cutting concerns.

```text
ecommerce-backend/
├── src/
│   ├── main/
│   │   ├── java/com/akbar/ecommerce_backend/
│   │   │   ├── cart/
│   │   │   ├── category/
│   │   │   ├── common/
│   │   │   ├── config/
│   │   │   ├── exception/
│   │   │   ├── inventory/
│   │   │   ├── order/
│   │   │   ├── payment/
│   │   │   ├── product/
│   │   │   ├── security/
│   │   │   └── user/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .dockerignore
├── .gitignore
├── Dockerfile
├── compose.yaml
├── HELP.md
├── mvnw
├── mvnw.cmd
└── pom.xml
```

> The tree highlights the main modules. Individual controllers, services, repositories, DTOs, entities, and tests are organized within the relevant packages.

## Prerequisites

### For local development
- Java 17
- IntelliJ IDEA or another Java IDE
- Maven, or the Maven Wrapper included in the repository
- MySQL 8
- Redis

### For Docker Compose
- Docker Desktop installed and running on Windows, macOS, or Linux
- Docker Compose available through Docker Desktop

## Getting Started

### 1. Clone the Repository

Open PowerShell or a terminal and run:

```powershell
git clone https://github.com/mhdakbar-hash/ecommerce-backend.git
cd ecommerce-backend
```

If you already have the repository, open its project root — the directory containing `pom.xml`, `Dockerfile`, and `compose.yaml`.

### 2. Configure Environment Variables

The application reads database credentials and the JWT signing secret from environment variables rather than hardcoding them in `application.properties`.

The following values are **illustrative local-development examples only**. Replace them with your own values and never use these example credentials in production.

| Variable                     | Purpose                                          | Example                                            |
|------------------------------|--------------------------------------------------|----------------------------------------------------|
| `ECOMMERCE_DB_URL`           | JDBC connection URL                              | `jdbc:mysql://localhost:3306/ecommerce_backend_db` |
| `ECOMMERCE_DB_USERNAME`      | Database username                                | `root`                                             |
| `ECOMMERCE_DB_PASSWORD`      | Database password                                | `YOUR_PASSWORD`                                    |
| `ECOMMERCE_JWT_SECRET`       | JWT signing secret                               | A long, random, Base64-encoded secret              |
| `REDIS_HOST`                 | Redis hostname                                   | `localhost`                                        |
| `REDIS_PORT`                 | Redis port                                       | `6379`                                             |
| `DOCKER_MYSQL_ROOT_PASSWORD` | Root password for the Docker-only MySQL instance | A new local development password                   |

**Important:** `ECOMMERCE_DB_URL` and `ECOMMERCE_DB_PASSWORD` are used for a local run. The Docker Compose setup uses `DOCKER_MYSQL_ROOT_PASSWORD` and connects the application to a separate database named `ecommerce_backend_docker`.

Do not use your normal local database URL when configuring the Docker application.

#### Set variables for the current PowerShell session

For a local run, set the variables in IntelliJ's Run Configuration or in the PowerShell session from which you launch the application.

Example — use your own values:

```powershell
$env:ECOMMERCE_DB_URL = "jdbc:mysql://localhost:3306/ecommerce_backend_db"
$env:ECOMMERCE_DB_USERNAME = "root"
$env:ECOMMERCE_DB_PASSWORD = "YOUR_PASSWORD"
$env:ECOMMERCE_JWT_SECRET = "<your-long-random-secret>"
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
```

These assignments apply to the current PowerShell session and processes launched from it. They are examples, not commands that should be committed to source control.

For Docker Compose, configure its two required variables:

```powershell
$env:DOCKER_MYSQL_ROOT_PASSWORD = "<your-new-docker-db-password>"
$env:ECOMMERCE_JWT_SECRET = "<your-long-random-secret>"
```

Use the same JWT secret for local and Docker runs if you want tokens to be signed consistently across those environments.

### 3. Run with Docker Compose

Docker Compose starts three services:

- **app** — the Spring Boot application
- **mysql** — an isolated MySQL 8 container
- **redis** — a Redis container

#### Step 1: Open the project root

Make sure your terminal is inside the repository directory containing `compose.yaml`.

#### Step 2: Set the required variables

In PowerShell:

```powershell
$env:DOCKER_MYSQL_ROOT_PASSWORD = "<your-new-docker-db-password>"
$env:ECOMMERCE_JWT_SECRET = "<your-long-random-secret>"
```

Use your own values. The JWT secret should be long, random, and kept private.

#### Step 3: Build and start the services

```powershell
docker compose up -d --build
```

The first build may take several minutes because Docker downloads base images and builds the application image.

#### Step 4: Check the service status

```powershell
docker compose ps
```

Wait until MySQL and Redis report healthy and the application is running.

#### Step 5: View application logs

```powershell
docker compose logs -f app
```

Press `Ctrl+C` to stop following the logs. This does not stop the containers.

#### Step 6: Open Swagger UI

Visit:

**http://localhost:8080/swagger-ui/index.html#/**

#### Step 7: Stop the services

```powershell
docker compose down
```

This stops and removes the Compose containers and network. The named Docker MySQL volume is retained, so the Docker database data persists across restarts.

**Database safety:** The Docker application connects to `ecommerce_backend_docker` through the internal Compose hostname `mysql`. The container's MySQL port is published on `127.0.0.1:3307`, leaving the usual local MySQL port `3306` unchanged. Redis is available to the application through the internal Compose network and is not published to the host.

Avoid `docker compose down -v` unless you intentionally want to delete the Docker database volume and its data.

### 4. Run Locally from IntelliJ IDEA

Local development requires a running MySQL instance and Redis instance, with environment variables configured for the local database.

1. Open the cloned project in IntelliJ IDEA.
2. Ensure the Project SDK is Java 17 or another compatible JDK configured to compile the project for Java 17.
3. Confirm that Maven dependencies have finished importing.
4. Start your local MySQL and Redis services.
5. Create the local database and configure the environment variables described above.
6. Open `application.properties` and review the configuration keys. Do not put actual passwords or secrets into the file.
7. Open `EcommerceBackendApplication.java` and run its `main()` method.

The application uses the configured JDBC URL to connect to MySQL and the configured host and port to connect to Redis.

For a Maven build, the included wrapper can also be used:

```powershell
.\mvnw.cmd clean package
```

This command builds the project and executes its configured tests. Ensure that the required environment and test configuration are available before running it.

## API Documentation

Swagger UI provides interactive API documentation and allows you to inspect and test the available endpoints.

When the application is running locally, open:

**http://localhost:8080/swagger-ui/index.html#/**

Use Swagger to:
- Explore the available endpoints and request schemas.
- Register a user and authenticate.
- Inspect response codes and response bodies.
- Authorize protected requests with the access token where required.
- Test product, category, inventory, cart, order, and payment workflows.

The exact endpoints and required request bodies are defined by the application's OpenAPI documentation.

## Mock Payment Gateway

This project uses a **mock payment gateway**, not a real payment provider. Its deterministic outcomes are intentional:

| Payment method | Mock outcome |
|----------------|--------------|
| `CARD`         | `FAILED`     |
| `UPI`          | `SUCCESS`    |
| `NET_BANKING`  | `SUCCESS`    |

The `CARD` failure is intentional so both failure and success workflows can be tested without depending on a real banking system.

The payment workflow supports retrying a failed payment using the existing payment record. A failed payment does not automatically cancel its order, so a customer can retry. Refund behavior is also simulated and does not transfer real money.

This implementation is intended for development and workflow testing. A production system would require a real payment provider, secure payment integration, signature verification, idempotency controls, and provider-specific webhook handling.

## Security and Configuration Notes

- Access tokens and refresh tokens are used for authentication workflows.
- Refresh-token rotation and replay protection help prevent reuse of rotated tokens.
- Role-based authorization restricts protected operations.
- Sensitive configuration is read from environment variables.
- Redis-based rate limiting helps restrict repeated login requests.
- Product and category caching reduces repeated database reads.
- Centralized exception handling provides consistent API error responses.
- Logs should not contain passwords, JWTs, refresh tokens, or other sensitive credentials.

**Production note:** Example passwords in this document are for local development only. Use unique credentials, a strong random JWT signing secret, least-privilege database users, TLS where appropriate, and deployment-specific configuration in production.

## Testing

Automated tests are included in the project, and the current test suite has been run successfully during development.

Run the Maven Wrapper from the project root:

```powershell
.\mvnw.cmd test
```

Tests may require appropriate environment variables or test-specific configuration. The Docker MySQL database and the normal local development database are separate; keep that separation when configuring tests.

## Troubleshooting

### Docker Compose reports a missing environment variable

Set both required variables in the same PowerShell session before starting Compose:

```powershell
$env:DOCKER_MYSQL_ROOT_PASSWORD = "<your-new-docker-db-password>"
$env:ECOMMERCE_JWT_SECRET = "<your-long-random-secret>"
docker compose up -d --build
```

### The application cannot connect to MySQL

- Check the service status with `docker compose ps`.
- Inspect MySQL logs with `docker compose logs mysql`.
- Inspect application logs with `docker compose logs app`.
- Confirm the Compose database URL uses `mysql:3306/ecommerce_backend_docker`.
- For a local run, confirm that MySQL is running and the local JDBC URL, username, and password are correct.

### Swagger UI does not open

- Confirm the application is running.
- Inspect application logs for startup errors.
- Verify that port `8080` is available and mapped correctly.
- Open `http://localhost:8080/swagger-ui/index.html#/`.

### Redis-related errors occur

- For Docker, inspect `docker compose ps` and `docker compose logs redis`.
- For a local run, confirm Redis is running and `REDIS_HOST` and `REDIS_PORT` are configured correctly.

### Port 8080 is already in use

Stop the other application using that port, or adjust the host port mapping in `compose.yaml` and access the application through the updated host port.

## Future Improvements

Potential areas for further development include:
- Real Payment Gateway Integration: Replace the mock payment gateway with a provider such as Razorpay or Stripe.
- Automated CI/CD Pipeline: Automate build verification, test execution, and deployment workflows.
- API Versioning and Backward Compatibility: Introduce a versioning strategy to support future API changes without disrupting existing clients.
- Centralized Monitoring and Alerting: Add application health dashboards, performance metrics, and alerts for operational issues.

---

