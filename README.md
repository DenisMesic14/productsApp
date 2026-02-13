# Product webshop

Spring Boot REST API for Product e-commerce platform.

## Prerequisites

- Java 17 or higher
- Maven 3.8+

## Installation

```bash
cd productsApp
mvn clean install
```

## Running the Application

```bash
mvn spring-boot:run
```

Application starts on: `http://localhost:8086`

## API Documentation

Swagger UI: `http://localhost:8086/swagger-ui/index.html`

## H2 Database Console

URL: `http://localhost:8086/h2-console`

Connection:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## Test User

A default user is created on startup:
- Username: `testuser`
- Password: `password123`

## Testing

Run all tests:
```bash
mvn test
```

Run specific test:
```bash
mvn test -Dtest=ProductServiceTest
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login (Basic Auth)

### Products
- `GET /api/products` - Get products (pagination, sorting)
- `GET /api/products/{id}` - Get single product
- `GET /api/products/search?q={query}` - Search products

### Cart
- `GET /api/cart` - Get user cart
- `POST /api/cart` - Add to cart
- `DELETE /api/cart/{productId}` - Remove from cart
- `DELETE /api/cart` - Clear cart

### Favorites
- `GET /api/favorites` - Get user favorites
- `POST /api/favorites/{productId}` - Add to favorites
- `DELETE /api/favorites/{productId}` - Remove from favorites

### User
- `GET /api/users/me` - Get current user info

## Configuration

File: `src/main/resources/application.properties`

```properties
server.port=8086
spring.datasource.url=jdbc:h2:mem:testdb
dummyjson.base-url=https://dummyjson.com
```

## Project Structure

```
src/main/java/hr/abysalto/hiring/mid/
├── components/          # Database initialization
├── configuration/       # Spring config (Security, CORS, Cache, Swagger)
├── controller/          # REST endpoints
├── domain/              # Entity models
├── dto/                 # Request/Response DTOs
├── repository/          # Data access layer
├── security/            # Custom UserDetailsService
└── service/             # Business logic
```

## Features

- User authentication with BCrypt
- Product catalog with DummyJSON integration
- Shopping cart management
- Favorites system
- Spring Security with Basic Auth
- CORS configured for frontend
- H2 in-memory database
- Batch query optimization (N+1 prevention)
- API caching
- Swagger documentation
- Comprehensive test suite

## Technologies

- Spring Boot 3.4.3
- Spring Security
- Spring Data JDBC
- H2 Database
- Lombok
- Jackson
- Swagger/OpenAPI 3
- JUnit 5 + Mockito

## Notes

- In-memory database resets on restart
- DummyJSON provides mock product data
- Basic Auth for simplicity (JWT recommended for production)

## Author

Denis Mesic
