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

### Layered Architecture Pattern
The application follows a strict layered architecture:
1. **API Layer** (`/api/v2/*`): REST controllers with OpenAPI 3.0 documentation
2. **Service Layer**: Transactional business logic with Redis caching
3. **Repository Layer**: Dual approach - JPA for simple operations, MyBatis for complex queries
4. **Domain Layer**: JPA entities with proper relationship mapping

### Data Access Strategy
- **JPA Repositories**: For basic CRUD operations and simple queries with method name conventions
- **MyBatis Mappers**: For complex queries requiring joins, filtering, and pagination (especially `LedgerMapper.xml`)
- **Performance Optimization**: MyBatis XML mappers handle complex search conditions and pagination efficiently
- **Query Monitoring**: P6Spy logs all SQL queries for performance analysis

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

### Key Entities and Relationships
- **Member**: User accounts with OAuth2 support (Google, Naver)
  - Primary key: `member_id`
  - Supports both social login and traditional authentication
- **Book**: Household budget books that can be shared among multiple users
  - Primary key: `book_id`
  - Owned by a Member, but can be shared with role-based access
- **UserBook**: Many-to-many join table managing book sharing and roles
  - Defines user permissions: OWNER, EDITOR, VIEWER
  - Enables collaborative household budget management
- **Ledger**: Individual budget entries/transactions
  - Links to Member (who created), Book (which book), Category, PaymentMethod
  - Supports amount types: INCOME, EXPENSE, TRANSFER
- **Category**: Expense/income categories with hierarchical structure
- **PaymentMethod**: Payment methods (cash, card, bank transfer)
- **FCM Notification System**: Complex multi-table notification system
  - Supports push notifications, SMS, and web notifications
  - Comprehensive audit trail and delivery tracking

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

## Security Architecture
- **OAuth2 + JWT Authentication**:
  - Access tokens (10 min expiry) + Refresh tokens (24 hours)
  - Social login integration (Google, Naver)
  - Token storage in Redis for session management
- **Database Security**: SSH tunneling through EC2 jump host to AWS RDS
- **CORS Configuration**: Multi-origin support for different client applications
- **Role-Based Access Control**: Book-level permissions (OWNER, EDITOR, VIEWER)
- **API Security**: JWT Bearer token required for protected endpoints
- **Firebase FCM**: Secure push notification delivery

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

## Development Patterns and Best Practices

### Service Layer Patterns
- **Transactional Services**: All service methods use `@Transactional` for data consistency
- **Current User Context**: Services use `SecurityContextHolder` to get current authenticated user
- **Redis Caching**: Frequently accessed data (like user book lists) cached in Redis
- **DTO Pattern**: Services convert between entities and DTOs for API responses

### Repository Pattern Usage
- **JPA Repositories**: Use Spring Data JPA method naming conventions for simple queries
- **MyBatis for Complex Queries**: Use MyBatis XML mappers for complex filtering and pagination
- **Query Optimization**: Critical queries (like ledger search) use MyBatis for performance

### Error Handling Patterns
- **Validation**: Jakarta Bean Validation with custom error messages
- **Exception Handling**: Proper HTTP status codes and meaningful error responses
- **Security Context**: Authentication errors properly handled and logged

### Testing Patterns
- **Unit Tests**: Mock external dependencies (repositories, FCM services)
- **Integration Tests**: Use `@SpringBootTest` for full integration testing
- **Performance Tests**: k6 scripts for load testing with realistic scenarios

## Important Files and Locations
- **Main config**: `src/main/resources/application.yml`
- **MyBatis mappers**: `src/main/resources/mapper/*.xml` (especially LedgerMapper.xml for complex queries)
- **Docker configs**: `docker-compose.yml`, `docker-compose.k6.yml`
- **Kubernetes manifests**: `src/main/resources/k8s/`
- **Monitoring configs**: `prometheus/`, `grafana/`, `logstash/`, `filebeat/`
- **Performance tests**: `k6-scripts/`
- **Key dependencies**: Spring Boot 3.4.5, Java 21, MySQL 8.0, Redis, Firebase FCM, JWT, OAuth2