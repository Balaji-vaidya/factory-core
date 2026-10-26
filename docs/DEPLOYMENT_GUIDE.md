# FACTORY CORE — DEPLOYMENT & OPERATIONS GUIDE
**Indo American Equipment / Agrisep®**  
*Factory Management & Inventory Management System*

---

## 1. System Requirements & Prerequisites

### 1.1 Local Development Environment
- **Operating System**: Windows 10/11, Ubuntu 22.04 LTS, or macOS 14+
- **Java**: Eclipse Temurin or Oracle JDK 21 (LTS)
- **Node.js**: v20.x or v22.x LTS (with npm 10+)
- **Build Tools**: Apache Maven 3.9+ or included Maven Wrapper (`./mvnw` / `mvnw.cmd`)
- **Container Engine**: Docker Desktop or Docker Engine 24+ with Docker Compose v2+
- **Database**: MySQL 8.0+ (InnoDB)

---

## 2. Configuration & Environment Variables

All sensitive values must be injected via environment variables. Never hardcode credentials.

### Backend Configuration (`backend/.env` or OS Environment)
| Variable | Default Value | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | Port for Spring Boot REST API |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/factory_core?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | MySQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `factory_user` | MySQL user |
| `SPRING_DATASOURCE_PASSWORD` | `factory_secret_password` | MySQL password |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` | 256-bit HMAC secret key |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` | 15 minutes (in milliseconds) |
| `JWT_REFRESH_EXPIRATION_MS`| `604800000` | 7 days (in milliseconds) |
| `STORAGE_LOCAL_ROOT` | `./uploads` | Local file uploads directory |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Permitted web clients |

### Frontend Configuration (`frontend/.env`)
| Variable | Default Value | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1` | Backend REST API endpoint |

---

## 3. Running with Docker Compose (Recommended for Local Full Stack)

A complete local production-like stack can be launched with Docker Compose:

```bash
# 1. Clone repository and navigate to root
cd d:\WORK\Project\Agrisep(jalna)

# 2. Start MySQL and Backend
docker compose up -d mysql

# 3. Verify MySQL is healthy
docker compose ps

# 4. Launch backend and frontend containers
docker compose up -d
```

### Stopping the Stack
```bash
docker compose down
# To preserve database data, do NOT use -v (volumes).
```

---

## 4. Running Directly from Source (Development Mode)

### Step 1: Start MySQL Database
Ensure a local MySQL 8 instance is running and create the database:
```sql
CREATE DATABASE IF NOT EXISTS factory_core CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'factory_user'@'%' IDENTIFIED BY 'factory_secret_password';
GRANT ALL PRIVILEGES ON factory_core.* TO 'factory_user'@'%';
FLUSH PRIVILEGES;
```

### Step 2: Start Spring Boot Backend
```bash
cd backend
# Using Maven Wrapper (Windows PowerShell)
.\mvnw.cmd clean spring-boot:run

# Or with installed Maven:
mvn clean spring-boot:run
```
Flyway will automatically execute migrations `V1` through `V10` upon startup.  
Verify backend health: `curl http://localhost:8080/actuator/health` -> `{"status":"UP"}`.  
OpenAPI Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

### Step 3: Start React Frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:3000` or `http://localhost:5173` in your browser.

---

## 5. Production Build & Packaging

### 5.1 Backend Production JAR
```bash
cd backend
mvn clean package -DskipTests=false
# Outputs: target/factory-core-1.0.0.jar
```
Execute standalone jar:
```bash
java -jar -Dspring.profiles.active=prod target/factory-core-1.0.0.jar
```

### 5.2 Frontend Production Bundle
```bash
cd frontend
npm run build
# Outputs optimized static assets to frontend/dist/
```

---

## 6. Database Backup & Disaster Recovery

### Daily Logical Backup (mysqldump)
```bash
mysqldump -u factory_user -p \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  factory_core > /backup/factory_core_$(date +%Y%m%d_%H%M%S).sql
```

### Restore Procedure
```bash
mysql -u factory_user -p factory_core < /backup/factory_core_YYYYMMDD_HHMMSS.sql
```
After restore, verify table row counts and rerun backend health check.

---

## 7. Security Hardening Checklist for Production

- [ ] Disable default administrative credentials after first login.
- [ ] Enforce TLS/HTTPS on Nginx reverse proxy with valid certificates (Let's Encrypt / DigiCert).
- [ ] Ensure `ddl-auto` is set to `validate` (never `create` or `update`).
- [ ] Restrict `CORS_ALLOWED_ORIGINS` to exact corporate domain URLs.
- [ ] Enable rate-limiting on public website enquiry endpoints.
- [ ] Configure automatic log rotation (`logback-spring.xml`) with 90-day retention for audit compliance.
