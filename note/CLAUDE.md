# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Run
```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Run with Docker Compose (includes monitoring stack)
docker-compose up -d

# Run only the Spring application
docker-compose up spring-app
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests MemberServiceTest

# Run integration tests
./gradlew integrationTest
```

### Performance Testing
```bash
# Run k6 load tests
docker-compose -f docker-compose.k6.yml up -d
docker-compose -f docker-compose.k6.yml logs -f k6
docker-compose -f docker-compose.k6.yml down -v
```

### Docker Operations
```bash
# Build Docker image
docker build -t garabuserver:latest .

# Run full monitoring stack
docker-compose up -d  # Includes ELK, Prometheus, Grafana

# Access monitoring dashboards
# Grafana: http://localhost:3000 (admin/admin123)
# Kibana: http://localhost:5601
# Prometheus: http://localhost:9090
```

## Architecture Overview

### Core Application Structure
This is a Spring Boot 3.4.5 application using Java 21, structured as a multi-layered REST API for a collaborative household budget management system ("가라부" - Garabu).

**Key Architectural Patterns:**
- **API Layer**: REST controllers in `api/` package handle HTTP requests
- **Service Layer**: Business logic in `service/` and `service/impl/` packages
- **Repository Layer**: Data access via Spring Data JPA repositories and MyBatis mappers
- **Domain Layer**: JPA entities representing core business objects
- **Security Layer**: OAuth2 + JWT authentication with Spring Security

### Domain Model (Key Entities)
- **Member**: User accounts with OAuth2 social login (Google, Naver)
- **Book**: Household budget books that can be shared among multiple users
- **UserBook**: Junction entity managing user permissions for shared books
- **Ledger**: Individual expense/income records within books
- **Category**: Expense categorization system
- **PaymentMethod**: Payment methods for transactions

### Multi-User Collaboration System
The application supports shared budget books through the **UserBook** entity system, allowing multiple users to collaborate on the same budget with role-based permissions.

### Authentication & Security
- **OAuth2 Social Login**: Google and Naver integration
- **JWT Tokens**: Stateless authentication with refresh token support
- **Spring Security**: Role-based access control
- **SSH Tunneling**: Secure database connections to AWS RDS

### Database Architecture
- **Primary Database**: MySQL 8.0 on AWS RDS
- **Caching Layer**: Redis for session management and performance optimization
- **Data Access**: Hybrid approach using both JPA (for CRUD operations) and MyBatis (for complex queries)
- **Connection Security**: SSH tunnel configuration for secure RDS access

### Monitoring & Observability Stack
- **ELK Stack**: Elasticsearch, Logstash, Kibana for log aggregation and analysis
- **Prometheus + Grafana**: Metrics collection and visualization
- **Spring Boot Actuator**: Application health and metrics endpoints
- **P6Spy**: SQL query monitoring and performance analysis
- **Structured Logging**: ECS-formatted logs via Logback

### External Integrations
- **Firebase FCM**: Real-time push notifications for budget updates
- **Slack Integration**: Log-based alerts and deployment notifications
- **AWS Services**: EC2 hosting, RDS database, potential EKS migration

### Configuration Management
- **Main Config**: `application.yml` for database, OAuth2, JWT, and monitoring settings
- **Security Config**: `SecurityConfig.java` for authentication and authorization
- **Database Config**: `SshDataSourceConfig.java` for SSH tunnel setup
- **Firebase Config**: `FirebaseConfig.java` for push notification setup

### Performance Considerations
- **Redis Caching**: Implemented for frequently accessed data
- **Database Indexing**: Optimized for common query patterns
- **Connection Pooling**: Configured for high-concurrency scenarios
- **JVM Tuning**: G1GC configuration for optimal performance

### Development & Deployment
- **CI/CD**: GitHub Actions for automated testing and deployment
- **Containerization**: Docker with multi-stage builds
- **Kubernetes**: Ready for EKS deployment with manifests in `k8s/`
- **Environment Management**: Development via Docker Compose, production on AWS EC2

### API Design
- **RESTful Endpoints**: Consistent `/api/v2/` prefix for all endpoints
- **JWT Authentication**: Required for all endpoints except registration
- **Swagger Documentation**: Available at `/swagger-ui/index.html`
- **Pagination Support**: Implemented for list endpoints with filtering capabilities

### Key Service Patterns
- **Transactional Services**: Business operations are wrapped in Spring transactions
- **DTO Pattern**: Separate DTOs for API requests/responses vs domain entities
- **Repository Pattern**: Abstraction layer for data access operations
- **Service Implementation**: Interface-based service layer with concrete implementations