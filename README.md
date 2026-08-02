# 🛒 E-Commerce Platform — Full Stack Java

> Production-ready Online Shopping System built with **Java 17 + Spring Boot 3** (backend) and **React.js** (frontend).

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Design Patterns](#design-patterns)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Deployment](#deployment)

---

## ✨ Features

### Customer Features
- 🔐 JWT Authentication (Register, Login, Token Refresh)
- 🛍️ Browse products by category, brand, price
- 🔍 Full-text search with filters and sort
- 🛒 Shopping cart with quantity management
- ❤️ Wishlist management
- 💳 Checkout with multiple address support
- 📦 Order tracking with status history
- ⭐ Product reviews and ratings
- 👤 Profile and address management
- 🎫 Discount coupon system

### Admin Features
- 📊 Sales analytics dashboard
- 📦 Product, category, brand management
- 👥 User management
- 🚚 Order and shipment management
- 🎁 Coupon creation and management
- ⚠️ Low stock alerts

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (JJWT 0.11.5) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8.0 |
| Build Tool | Maven 3.9+ |
| Documentation | SpringDoc OpenAPI 3 (Swagger) |
| PDF Generation | iText 7 |
| Frontend | React.js (Vite) |
| UI | Tailwind CSS |
| State Management | Redux Toolkit |
| HTTP Client | Axios |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│                  React.js Frontend               │
│          (Vite + Redux + Tailwind CSS)           │
└──────────────────┬──────────────────────────────┘
                   │  HTTP / REST / JSON
                   │  Authorization: Bearer <JWT>
┌──────────────────▼──────────────────────────────┐
│              Spring Boot Backend                 │
│  ┌─────────┐ ┌─────────┐ ┌─────────────────┐   │
│  │Controller│→│ Service │→│  Repository     │   │
│  └─────────┘ └─────────┘ └────────┬────────┘   │
│       ↑              ↑            │             │
│     DTOs          Patterns       JPA            │
│  (Request/       (Strategy,      │             │
│   Response)      Observer, ...)  │             │
└──────────────────────────────────┼─────────────┘
                                   │
                        ┌──────────▼──────────┐
                        │   MySQL Database     │
                        │  (ecommerce_db)      │
                        └─────────────────────┘
```

### Layered Package Structure
```
com.ecommerce/
├── config/          — Security, Swagger, CORS configuration
├── controller/      — REST controllers (@RestController)
├── dto/             — Data Transfer Objects (request/response)
│   ├── request/     — Incoming payloads (with @Valid)
│   └── response/    — Outgoing payloads (safe, no passwords)
├── entity/          — JPA entities (@Entity)
├── exception/       — Custom exceptions + GlobalExceptionHandler
├── repository/      — Spring Data JPA repositories
├── security/        — JWT utility, filter, UserDetailsService
└── service/         — Business logic layer
```

---

## 📁 Project Structure

```
ecommerce-cog/
├── backend/                    ← Spring Boot Maven project
│   ├── pom.xml
│   ├── mvnw.cmd                ← Maven wrapper (no Maven install needed)
│   └── src/
│       ├── main/java/com/ecommerce/
│       └── test/java/com/ecommerce/
├── frontend/                   ← React.js (Phase 6)
├── sql/
│   └── schema.sql              ← Complete database schema
├── docs/                       ← ER diagrams, UML (TODO)
├── context.txt                 ← Phase tracker (current state + TODOs)
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites

| Tool | Version | Download |
|---|---|---|
| Java JDK | 17+ | [Adoptium](https://adoptium.net/) |
| MySQL | 8.0+ | [MySQL Downloads](https://dev.mysql.com/downloads/) |
| Node.js | 18+ | [Node.js](https://nodejs.org/) (for frontend) |

### 1. Database Setup

```sql
-- Connect to MySQL and run:
SOURCE sql/schema.sql;
```

### 2. Backend Configuration

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_db
    username: root        # ← change this
    password: root        # ← change this

jwt:
  secret: your-256-bit-secret-key-here   # ← MUST change in production
```

### 3. Run Backend

```bash
cd backend

# Option A: Using Maven Wrapper (no Maven install needed)
.\mvnw.cmd spring-boot:run

# Option B: If Maven is installed globally
mvn spring-boot:run
```

Backend starts at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### 4. Run Frontend (Phase 6)

```bash
cd frontend
npm install
npm run dev
```

Frontend starts at: `http://localhost:5173`

---

## 📖 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Register new user |
| `POST` | `/auth/login` | Login, get JWT tokens |
| `POST` | `/auth/refresh` | Refresh access token |

**Register Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass@123",
  "phone": "9876543210"
}
```

**Login Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "userId": 1,
    "email": "john@example.com",
    "fullName": "John Doe",
    "roles": ["ROLE_USER"]
  }
}
```

### User Profile

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/users/me` | ✅ | Get profile |
| `PATCH` | `/users/me` | ✅ | Update profile |
| `PUT` | `/users/me/password` | ✅ | Change password |
| `GET` | `/users/me/addresses` | ✅ | List addresses |
| `POST` | `/users/me/addresses` | ✅ | Add address |
| `PUT` | `/users/me/addresses/{id}` | ✅ | Update address |
| `DELETE` | `/users/me/addresses/{id}` | ✅ | Delete address |
| `PATCH` | `/users/me/addresses/{id}/default` | ✅ | Set default address |

> 📝 Full API documentation auto-generated at `/swagger-ui.html`

---

## 🎨 Design Patterns

| Pattern | Implementation | Location |
|---|---|---|
| **Singleton** | Spring beans, JwtUtil | `security/JwtUtil.java` |
| **Builder** | Entities, DTOs (Lombok @Builder) | All entities, DTOs |
| **Factory Method** | `ApiResponse.success()`, `ApiResponse.error()` | `dto/response/ApiResponse.java` |
| **Chain of Responsibility** | JWT filter chain | `security/JwtAuthenticationFilter.java` |
| **Facade** | AuthService, UserService | `service/` |
| **Strategy** | Payment processing (Phase 4) | `service/payment/` |
| **Observer** | Order notifications (Phase 4) | `service/observer/` |
| **State** | Order status machine (Phase 4) | `service/order/` |
| **Decorator** | Price calculation - tax+shipping (Phase 3) | `service/cart/` |
| **Template Method** | Order processing pipeline (Phase 4) | `service/order/` |

---

## 🗄️ Database Schema

21 tables covering all e-commerce needs:

```
users ──────── user_roles ─── roles
  │
  ├── addresses
  ├── carts ───── cart_items ─── products
  │                              │
  ├── wishlists ─── wishlist_items  ├── product_images
  │                              ├── product_variants
  ├── orders ──── order_items   ├── product_specifications
  │    │                        └── inventory
  │    ├── payments
  │    ├── shipments
  │    └── order_status_history
  │
  └── reviews
```

---

## 🧪 Testing

```bash
# Run all tests
.\mvnw.cmd test

# Run specific test class
.\mvnw.cmd test -Dtest=AuthServiceTest
```

Test coverage:
- ✅ `AuthServiceTest` — register, duplicate email

---

## 🚢 Deployment

### Environment Variables (Production)

```bash
JWT_SECRET=<256-bit-base64-encoded-key>
MAIL_USERNAME=smtp-user@yourdomain.com
MAIL_PASSWORD=your-smtp-password
```

### Build JAR

```bash
cd backend
.\mvnw.cmd clean package -DskipTests
java -jar target/ecommerce-backend-1.0.0.jar
```

---

## 📈 Development Progress

| Phase | Status | Description |
|---|---|---|
| Phase 1 | ✅ Complete | Setup, JWT Auth, User/Address management |
| Phase 2 | 🔄 Next | Product, Category, Brand, Inventory |
| Phase 3 | ⏳ Planned | Cart, Wishlist, Coupons |
| Phase 4 | ⏳ Planned | Orders, Checkout, Payments |
| Phase 5 | ⏳ Planned | Reviews, Admin Dashboard |
| Phase 6 | ⏳ Planned | React.js Frontend |

> 📋 See [`context.txt`](context.txt) for detailed phase tracker

---

## 🔒 Security

- **JWT Authentication** — short-lived access tokens (24h) + refresh tokens (7d)
- **BCrypt(12)** — industry-standard password hashing with salt
- **RBAC** — `ROLE_USER` / `ROLE_ADMIN` with method-level `@PreAuthorize`
- **CORS** — restricted to configured origins only
- **Input Validation** — Bean Validation on all request DTOs
- **SQL Injection** — prevented by JPA/Hibernate parameterized queries
- **No password exposure** — DTOs never include the password field

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.
