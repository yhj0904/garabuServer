# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.4.5 application called "가라부 (Garabu)" - a household budget management backend API server. It supports multi-user collaboration, real-time notifications, and comprehensive monitoring capabilities.

## Build and Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests (JUnit 5)
./gradlew test

# Run specific test
./gradlew test --tests MemberServiceTest

# Clean build
./gradlew clean build

# Check dependencies
./gradlew dependencies
```

### Docker Commands
```bash
# Build Docker image
docker build -t garabuserver:latest .

# Run with Docker Compose (includes monitoring stack)
docker-compose up -d

# Run only the application
docker-compose up spring-app

# Performance testing with k6
docker-compose -f docker-compose.k6.yml up -d

# Available k6 test scripts
k6 run k6-scripts/loadtest.js       # Main load test
k6 run k6-scripts/stages/smoke.js   # Smoke test
k6 run k6-scripts/stages/soak.js    # Soak test
```

## Architecture Overview

### Key Components
- **Spring Boot 3.4.5** with Java 21
- **JPA + MyBatis hybrid approach**: JPA for simple CRUD, MyBatis for complex queries
- **OAuth2 + JWT authentication** (Google, Naver social login)
- **Redis caching** for session management and performance
- **Firebase FCM** for push notifications
- **MySQL 8.0** on AWS RDS with SSH tunneling
- **Comprehensive monitoring**: ELK Stack + Prometheus + Grafana

### Package Structure
```
src/main/java/garabu/garabuServer/
├── api/                    # REST API controllers (v2)
├── config/                 # Configuration classes
├── controller/             # Additional controllers
├── domain/                 # JPA entities
├── dto/                    # Data Transfer Objects
├── jwt/                    # JWT authentication
├── oauth2/                 # OAuth2 configuration
├── repository/             # Data access layer (Spring Data JPA)
├── service/                # Business logic layer
└── mapper/                 # MyBatis mappers (not in package structure)
```

### Key Entities
- **Member**: User accounts with OAuth2 support
- **Book**: Household budget books that can be shared
- **UserBook**: Many-to-many relationship between users and books with roles
- **Ledger**: Budget entries/transactions
- **Category**: Expense/income categories
- **PaymentMethod**: Payment methods
- **FCM entities**: Push notification system

### Database Strategy
- **JPA**: Used for basic CRUD operations and entity relationships
- **MyBatis**: Used for complex queries, especially in LedgerMapper.xml for filtering and pagination
- **P6Spy**: SQL query monitoring for performance analysis

## Key Features
- Multi-user shared household budget books
- Real-time push notifications via Firebase FCM
- OAuth2 social login (Google, Naver)
- JWT-based authentication
- Redis caching for performance
- Comprehensive monitoring with ELK + Prometheus + Grafana
- SSH tunneling for secure AWS RDS access
- Performance testing with k6

## Configuration Notes
- Main config: `src/main/resources/application.yml`
- MyBatis mappers: `src/main/resources/mapper/`
- Docker configs: `docker-compose.yml`, `docker-compose.k6.yml`
- Kubernetes manifests: `src/main/resources/k8s/`
- Monitoring configs: `prometheus/`, `grafana/`, `logstash/`, `filebeat/`

## Testing
- Unit tests with JUnit 5
- Integration tests available
- Performance testing with k6 scripts in `k6-scripts/`
- API testing via Swagger UI at `/swagger-ui/index.html`

## Security
- OAuth2 + JWT authentication
- SSH tunneling for database access
- CORS configuration
- Spring Security for role-based access control
- Firebase configuration for push notifications

## Monitoring and Observability
- Prometheus metrics at `/actuator/prometheus`
- Structured logging with Logback
- ELK stack for log analysis
- Grafana dashboards for visualization
- P6Spy for SQL query monitoring

## Development Notes
- Uses Lombok for boilerplate code reduction
- Spring Boot DevTools for development
- Swagger/OpenAPI 3.0 for API documentation at `/swagger-ui/index.html`
- P6Spy for SQL query monitoring (logs all executed queries)
- Main entry point: `GarabuServerApplication.java`
- Application runs on port 8080 by default
- MyBatis XML mappers located in `src/main/resources/mapper/`
- Available test classes: MemberServiceTest, FcmSendServiceImplTest, CustomUserDetailsServiceTest, UserBookServiceTest
- Comprehensive README.md with detailed architecture and setup instructions

## Important Files and Locations
- **Main config**: `src/main/resources/application.yml`
- **MyBatis mappers**: `src/main/resources/mapper/*.xml` (especially LedgerMapper.xml for complex queries)
- **Docker configs**: `docker-compose.yml`, `docker-compose.k6.yml`
- **Kubernetes manifests**: `src/main/resources/k8s/`
- **Monitoring configs**: `prometheus/`, `grafana/`, `logstash/`, `filebeat/`
- **Performance tests**: `k6-scripts/`
- **Key dependencies**: Spring Boot 3.4.5, Java 21, MySQL 8.0, Redis, Firebase FCM, JWT, OAuth2