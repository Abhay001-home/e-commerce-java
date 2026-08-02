# E-Commerce Platform — Full Stack Java — Implementation Plan

## Overview

A production-ready, full-stack e-commerce platform built with **Java 17 + Spring Boot** (backend) and **React.js** (frontend). The project will be built in **6 structured phases**, each tracked in `context.txt`.

---

## Architecture Overview

```
ecommerce-cog/
├── backend/          ← Spring Boot Maven project
├── frontend/         ← React.js (Vite)
├── docs/             ← ER Diagram, UML, Swagger exports
├── sql/              ← Schema + seed scripts
├── context.txt       ← Phase tracker (what's done / what's left)
└── README.md
```

### Backend Architecture (Layered)
```
Controller → Service → Repository → Entity
              ↓
         DTOs / Mappers
              ↓
         Design Patterns (Strategy, Observer, State, Factory...)
```

---

## Phases

### Phase 1 — Project Setup & Foundation
- Spring Boot project scaffold (Maven, Java 17)
- MySQL database connection
- Global config: Security, CORS, Swagger/OpenAPI
- Base entities: User, Role, Address
- JWT Authentication (register, login, refresh)
- BCrypt password encryption
- Role-Based Access Control (ADMIN / USER)
- Global Exception Handling
- `context.txt` initialized

### Phase 2 — Product & Category Module
- Category CRUD (Admin)
- Brand management
- Product CRUD with images
- Product variants & specifications
- Inventory management
- Stock availability checks
- Search, filter, sort APIs
- Pagination support

### Phase 3 — Cart, Wishlist & Coupon
- Shopping Cart (add/remove/update/save for later)
- Tax & shipping charge calculation
- Wishlist (add/remove/move to cart)
- Coupon/Discount management
- Coupon validation and application

### Phase 4 — Order, Checkout & Payment
- Checkout flow (billing + shipping address)
- Order placement
- Order Status State Machine (Pending → Delivered → Cancelled → Returned)
- Payment module — Strategy Pattern (Cash on Delivery + extensible)
- Observer pattern for order notifications
- Invoice PDF generation
- Order tracking & history
- Cancel / Return order

### Phase 5 — Reviews, Admin Dashboard & Analytics
- Product Reviews & Ratings (CRUD)
- Average rating calculation
- Admin: user management, order management, shipment status
- Admin: Sales analytics (revenue, best sellers, low stock, pending orders)
- Email notification placeholders

### Phase 6 — Frontend (React.js)
- Project setup with Vite + React Router + Redux Toolkit
- Pages: Home, Login, Register, Product Listing, Product Detail
- Pages: Cart, Checkout, Payment, Orders, Wishlist, Profile
- Admin Dashboard with charts
- Axios integration with JWT interceptors
- Responsive UI with Tailwind CSS
- Loading indicators, pagination, form validation

---

## Design Patterns Map

| Pattern | Where Used |
|---|---|
| Singleton | Spring Beans, JWT Util |
| Factory Method | Payment strategy factory |
| Builder | Entity builders (Lombok @Builder) |
| Strategy | Payment processing |
| Observer | Order status notifications |
| State | Order status transitions |
| Command | Order operations |
| Template Method | Order processing pipeline |
| Chain of Responsibility | Auth filter chain |
| Decorator | Price calculation (tax + shipping) |
| Facade | OrderService facade |
| Adapter | Payment gateway adapters |

---

## Database Tables

Users, Roles, User_Roles, Addresses, Categories, Brands, Products, Product_Images, Product_Variants, Product_Specifications, Inventory, Cart, Cart_Items, Wishlists, Wishlist_Items, Coupons, Orders, Order_Items, Payments, Reviews, Shipments

---

## Open Questions

> [!IMPORTANT]
> **MySQL Connection**: Please confirm your MySQL host/port/database name, username and password (or confirm using defaults: localhost:3306/ecommerce_db, root/root).

> [!IMPORTANT]
> **Email**: Should email notifications be real (SMTP) or stubbed/logged for now?

> [!NOTE]
> **Payment**: Only Cash on Delivery is required now. Stripe/Razorpay can be added as a future enhancement via the Strategy pattern already in place.

---

## Verification Plan

- Run `mvn clean install` — zero errors
- Swagger UI accessible at `/swagger-ui.html`
- All REST endpoints tested via Swagger / Postman
- Frontend `npm run dev` with live API integration
- Unit tests with JUnit 5 for service layer

---

## context.txt Strategy

After each phase, `context.txt` will be updated with:
- ✅ Completed items (with file paths)
- ⏳ In-progress items
- 📋 Remaining phases and tasks
- 🔧 Known issues / TODOs

This allows any future model (or engineer) to pick up exactly where we left off.
