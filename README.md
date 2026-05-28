# 🧠 Synapse Backend API

Welcome to the backend engine of **Synapse**—a high-performance, secure, and modern neural workspace designed to coordinate thoughts, notes, visual canvases, and tasks. 

This backend is built on **Java 21** and **Spring Boot 3.4.3**, utilizing a polyglot persistence model to leverage the unique strength of multiple database engines.

---

## 🏛️ Architecture & Technology Stack

Unlike generic single-database architectures, Synapse operates on a **Polyglot Persistence** model to optimize performance, scalability, and transactional guarantees across different business domains:


flowchart TD
    Client[React Frontend] -->|REST/SSE| Gateway[Spring Boot API]
    
    Gateway --> Auth[Security & Session Layer]
    Gateway --> NotesSvc[Notes Canvas Service]
    Gateway --> TaskSvc[Tasks & Reminders Service]
    
    Auth -->|Caching & Token Blacklist| Redis[(Redis Cache)]
    NotesSvc -->|Visual Nodes & Hierarchies| Mongo[(MongoDB)]
    TaskSvc -->|ACID Transactions| MySQL[(MySQL)]
    
    subgraph Data Stores
        Redis
        Mongo
        MySQL
    end


### 🗄️ Database Mapping & Rationale

| Feature Area | Storage Engine | Rationale |
| :--- | :--- | :--- |
| **User Identity & Security** | **MySQL** | Enforces strict ACID compliance, relational integrity, unique email constraints, and direct references for user settings. |
| **Tasks & Reminders** | **MySQL** | Structured state, time-based indexes, and strict relational links to User IDs for atomic operations. |
| **Notes & Tag Canvas** | **MongoDB** | Visual notes require fluid unstructured schemas for rich-text contents, custom coordinates on a 2D canvas, and variable nesting tag lists. |
| **Session Cache & Rate Limiting** | **Redis** | Superb sub-millisecond lookups for blacklisted JWTs, IP rate-limiting, and temporary profile session caching. |

---

## ⚙️ Project Prerequisites

Ensure you have the following installed on your local machine before starting development:
* **Java Development Kit (JDK) 21**
* **Apache Maven 3.9+**
* **Docker & Docker Compose** (Highly recommended for zero-install database setup)

---

## 🚀 Getting Started

### 1. Environment Configurations (`.env`)
The backend is designed to run securely using environment-based secrets. In the `backend` root folder, create or edit your `.env` file (which is already pre-configured for local dev):

```env
GOOGLE_CLIENT_ID=your-google-oauth-client-id
GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret
JWT_SECRET=your-custom-secure-base64-jwt-signing-key
```
*(Note: These environment values are automatically mapped inside the Spring context and the Docker container configuration).*

---

### 2. Database Services (Docker Compose Setup)
You can start all databases (MySQL, MongoDB, Redis) instantly using the pre-configured `docker-compose.yml` file in the backend root:

```bash
# Spin up MySQL, MongoDB, and Redis in the background
docker compose up -d mysql mongodb redis
```

This starts:
- **MySQL** on port `3307` (internally mapped to `3306` inside container)
- **MongoDB** on port `27017`
- **Redis** on port `6379`

---

### 3. Run the Spring Boot Application
Once the databases are healthy and running, start the Spring Boot backend server:

```bash
# Navigate to the backend directory and run
mvn spring-boot:run
```
The server will boot up and start listening on **`http://localhost:8080`**.

---

## 🐳 Running the Entire Stack via Docker Compose
To build and run the entire Synapse backend—including the Spring Boot application container—as a unified container stack:

```bash
# Build and start all services (App + MySQL + MongoDB + Redis)
docker compose up --build
```
This runs the backend under the `prod` profile. The container automatically connects to the networked database services on the private bridge network.

---

## 📖 API Documentation & Swagger

The API is fully documented using Swagger UI. Once the application is running, you can interact with and test all the endpoints through the browser:

* **Swagger UI Interactive Panel:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Raw Specification:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🛡️ Security & Authentication Flow

1. **Stateless JWT:** Synapse uses stateless JWT token-based authentication. On successful login, the client receives a secure Access Token and a rotated Refresh Token.
2. **Google OAuth2 Support:** Includes native Google Social OAuth2 login flows via Spring Security.
3. **Stateless Token Blacklist:** Token revocation (during sign-out) is tracked instantly using high-speed Redis key expiration counters, preventing stale token reuse.

---

## 📂 Source Code Structure

```text
backend/src/main/java/com/synapse/backend/
├── config/             # Spring Security, CORS, Mongo, and Redis configurations
├── controller/         # REST Controllers versioned under /api/v1/...
├── dto/                # Request validation schemas and Response schemas
├── exception/          # Custom exceptions and Global RFC 7807 problem handler
├── model/              # Domain models (JPA Entities and MongoDB Documents)
├── repository/         # Spring Data JPA, Mongo, and Redis repositories
├── security/           # JWT Filter pipeline and OAuth2 success handlers
├── service/            # Business logic implementations
└── util/               # Utility classes and helper functions
```

---
*For frontend architecture, please refer to the main frontend module documentation.*
