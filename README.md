# FACTORY CORE — Factory Management & Inventory Management System
**Indo American Equipment / Agrisep®**  
*Seed-Processing Machinery & Plants Manufacturer — Jalna, Maharashtra, India*

---

## 1. Project Overview

**Factory Core** is an enterprise-grade Factory Management and Inventory Management System designed and built for **Indo American Equipment / Agrisep®**, a premier manufacturer of industrial seed-processing machinery, gravity separators, pre-cleaners, destoners, and turnkey seed conditioning plants based in Jalna, Maharashtra.

Factory Core is a full-stack, modular monolith business management platform replacing spreadsheet-based workflows with strict mathematical inventory invariants, sequential transactional numbering, multi-role authorization, deterministic FIFO allocation, immutable audit logging, and delivery tracking.

---

## 2. Technology Stack

### Backend
- **Language & Runtime**: Java 21 LTS
- **Framework**: Spring Boot 3.4.x / 4.x
- **Security**: Spring Security with cryptographic HMAC-SHA256 JWT tokens & BCrypt password hashing
- **Persistence**: Spring Data JPA & Hibernate ORM 7.x
- **Database**: MySQL 8.0+ (InnoDB) with UTC timestamp storage & UTF-8mb4 character set
- **Database Migrations**: Flyway (Migrations `V1` through `V10`)
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **Testing**: JUnit 5, Mockito, Spring Boot Test, H2 in-memory test database

### Frontend
- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite 6+
- **Styling**: Tailwind CSS & Lucide React icons
- **State & Data Fetching**: TanStack Query (React Query) & Axios
- **Form Handling & Validation**: React Hook Form & Zod
- **Navigation**: React Router v7
- **UI Components**: Modern industrial B2B design system (Dark navy primary `#1E3A8A`, neutral gray background `#F8FAFC`, high contrast status badges)

### Infrastructure & DevOps
- **Containerization**: Multi-stage production Dockerfiles for Backend & Frontend
- **Orchestration**: Docker Compose (`docker-compose.yml`)
- **Reverse Proxy**: Nginx Alpine with SPA routing and API proxying
- **CI/CD**: GitHub Actions automated pipeline (`.github/workflows/ci.yml`)

---

## 3. Architecture & Project Layout

```
Agrisep(jalna)/
├── backend/                             # Spring Boot 3.x Java 21 Backend
│   ├── src/main/java/com/factory/
│   │   ├── audit/                       # Tamper-evident Audit Trail
│   │   ├── auth/                        # JWT Auth, UserDetailsService, Security Evaluator
│   │   ├── client/                      # Client Master, Addresses & Snapshot Freezing
│   │   ├── common/                      # Config, Exceptions, Envelopes, Sequences
│   │   ├── delivery/                    # Delivery Challans (DC), Dispatch & Proof of Delivery
│   │   ├── document/                    # Secure File Storage Vault
│   │   ├── enquiry/                     # Public Website Leads & 1-Click Conversion
│   │   ├── inventory/                   # Stock Balances, Ledger & Deterministic FIFO Allocation
│   │   ├── notification/                # In-App Notifications & Unread Counter
│   │   ├── office/                      # Multi-Office & Godown Entity Management
│   │   ├── po/                          # Purchase Orders, Versions & Line Items
│   │   ├── product/                     # Machinery Models, Specs & SKU Immutability
│   │   ├── reporting/                   # Role Dashboards & Multi-Format CSV Reports
│   │   └── user/                        # User Management & Account Lockout
│   ├── src/main/resources/
│   │   ├── db/migration/                # Flyway Migrations V1 through V10
│   │   ├── application.yml              # Base Application Configuration
│   │   ├── application-local.yml        # Development & Testing Profile
│   │   └── application-prod.yml         # Production MySQL Profile
│   ├── src/test/java/com/factory/       # 12 Integration Test Suites (59 Tests)
│   ├── Dockerfile                       # Multi-stage Eclipse Temurin JDK 21 Alpine
│   └── pom.xml                          # Maven Project Definition
│
├── frontend/                            # React 19 + TypeScript + Vite Frontend
│   ├── src/
│   │   ├── components/                  # Design System Components (Badges, Modals, Tables)
│   │   ├── features/                    # Domain Features (Auth, Clients, Products, POs, etc.)
│   │   ├── services/                    # Typed Axios API Client Layer (auth, po, dc, etc.)
│   │   ├── types/                       # Shared TypeScript Interfaces & Enums
│   │   ├── App.tsx                      # Root Application & Navigation Router
│   │   └── main.tsx                     # Entry Point & QueryClientProvider
│   ├── nginx.conf                       # Production Nginx SPA & Proxy Configuration
│   ├── Dockerfile                       # Multi-stage Node 22 + Nginx Alpine
│   ├── package.json                     # Frontend Dependencies
│   └── vite.config.ts                   # Vite Configuration with API Proxy
│
├── docs/                                # Enterprise System Documentation
│   ├── ARCHITECTURE.md                  # Modular Monolith Architectural Blueprint
│   ├── IMPLEMENTATION_PLAN.md           # 15-Phase Roadmap & Delivery Audit Log
│   ├── DATABASE_DESIGN.md               # Relational Schema, Invariants & ER Diagrams
│   ├── API_SPECIFICATION.md             # Complete REST API Endpoint Specification
│   ├── TESTING_PLAN.md                  # Quality Assurance & Test Execution Reports
│   └── DEPLOYMENT_GUIDE.md              # Operations Manual, Backups & Runbooks
│
├── .github/workflows/ci.yml             # GitHub Actions CI/CD Pipeline
├── docker-compose.yml                   # Root Docker Compose Orchestration
└── README.md                            # System Documentation
```

---

## 4. Key Business Modules & Workflows

### 4.1 Strict Mathematical Inventory Invariants
The inventory engine prevents stock overdrafts with database `CHECK` constraints and pessimistic row-level locking (`SELECT ... FOR UPDATE`):
$$\text{ON\_HAND} \ge 0$$
$$\text{RESERVED} \ge 0$$
$$\text{RESERVED} \le \text{ON\_HAND}$$
$$\text{AVAILABLE} = \text{ON\_HAND} - \text{RESERVED}$$
Every stock movement records an immutable entry into `stock_ledger` (`INWARD`, `PO_RESERVATION`, `PO_UNRESERVATION`, `DC_DISPATCH`, `MANUAL_ADJUSTMENT`, `RETURN_INWARD`). Manual stock adjustments mandate written audit justifications.

### 4.2 Sequential Transactional Numbering
All documents are numbered using thread-safe, database-pessimistic sequence generators:
- **Purchase Orders**: `PO-000001`, `PO-000002`, ...
- **Delivery Challans**: `DC-000001`, `DC-000002`, ...

### 4.3 Purchase Order Lifecycle & Snapshot Freezing
- **Statuses**: `DRAFT` $\to$ `PENDING_OWNER_APPROVAL` $\to$ `APPROVED` $\to$ `PARTIALLY_DELIVERED` $\to$ `DELIVERED`. (Also supports `RETURNED`, `CANCEL_REQUESTED`, `CANCELLED`).
- **Snapshot Freezing**: Client billing details and machinery product specifications are frozen into immutable JSON snapshots at PO creation.
- **Relational Versioning**: Any subsequent modification to an approved PO creates a permanent `po_versions` snapshot with before-and-after change diffs and requires Owner re-approval.
- **SKU Immutability**: Once a product is referenced in any PO, its SKU code is permanently locked against changes (`ErrorCode.SKU_IMMUTABLE`).

### 4.4 Deterministic FIFO Shortage Allocation
When an approved PO demands stock exceeding warehouse availability:
1. Available stock is immediately reserved.
2. The remaining shortfall is registered in `stock_shortages` with status `OPEN`.
3. When new machinery is finished and inwarded into the godown, the engine queries open shortages using a deterministic priority algorithm:
   $$\text{ORDER BY } \text{po\_date ASC, expected\_delivery\_date ASC, po\_number ASC}$$
4. Inward stock is transactionally allocated to backlog shortages in strict chronological order, marking them `RESOLVED`.

### 4.5 Delivery Challans, Dispatch & Proof of Delivery
- **Over-Reservation Rejection**: A DC cannot be prepared for quantities exceeding the PO's currently reserved stock.
- **Atomic Dispatch**: When the truck departs the factory, physical stock is decremented ($\text{ON\_HAND} -= Q$, $\text{RESERVED} -= Q$), a `DC_DISPATCH` ledger entry is committed, and the PO transitions to `PARTIALLY_DELIVERED`.
- **Delivery Confirmation**: Upon client site unloading, recipient signatory name and phone are recorded. When all items are delivered, the PO automatically transitions to `DELIVERED`.

### 4.6 Multi-State Trade Scoping & Secrecy
Users are bound to primary and allowed offices. Personnel at `OUTER_STATE_OFFICE` locations (e.g. Gujarat or Andhra Pradesh branches) can manage their regional clients and POs, but are strictly prohibited from viewing physical warehouse inventory balances in Jalna (`403 Forbidden`).

### 4.7 Website Leads with 1-Click Client Conversion
Public inquiries submitted through the Agrisep website flow into an internal triage pipeline (`NEW` $\to$ `CONTACTED` $\to$ `QUALIFIED`). Sales managers can click **"Convert to Client"** to atomically register the prospect as a verified enterprise account with delivery addresses.

---

## 5. Seeded Personnel Accounts & Credentials

The system initializes with default personnel accounts for testing:

| Role | Email Address | Default Password | Assigned Office | Scoped Permissions |
|---|---|---|---|---|
| **OWNER** | `owner@agrisep.com` | `Owner@12345` | Jalna Head Office | Executive approvals, inventory adjustments, full audit logs |
| **ADMIN** | `admin@agrisep.com` | `Admin@12345` | Jalna Head Office | User management, offices, master catalog, operational support |
| **MAIN_OFFICE** | `mainoffice@agrisep.com` | `MainOffice@12345` | Jalna Head Office | Client management, PO creation, enquiry triage, reports |
| **OUTER_STATE_OFFICE** | `outerstate@agrisep.com` | `OuterState@12345` | Gujarat Regional Branch | Regional POs and clients (Godown inventory strictly concealed) |
| **GODOWN_MANAGER** | `godown@agrisep.com` | `Godown@12345` | Jalna Central Godown #1 | Stock inward receipts, DC generation, physical dispatch |

*Note: In production, default passwords must be rotated immediately via `PUT /api/v1/users/{id}`.*

---

## 6. Quickstart Guide

### Option A: Complete Stack via Docker Compose (Recommended)

To run the complete production stack (MySQL 8, Spring Boot Backend, React + Nginx Frontend):

```bash
# 1. Clone repository and navigate to root directory
cd Agrisep(jalna)

# 2. Launch all services
docker compose up -d

# 3. Check container health status
docker compose ps
```

- **Frontend Web Application**: [http://localhost:3000](http://localhost:3000)
- **Backend REST API**: [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
- **Actuator Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **OpenAPI / Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

To stop the containers:
```bash
docker compose down
```

---

### Option B: Local Development from Source

#### Prerequisites
- Java 21 LTS installed (`java -version`)
- Node.js 20+ or 22+ installed (`node -v`)
- MySQL 8.0 running locally on port 3306

#### Step 1: Start MySQL Database
```sql
CREATE DATABASE IF NOT EXISTS factory_core CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'factory_user'@'%' IDENTIFIED BY 'factory_secret_password';
GRANT ALL PRIVILEGES ON factory_core.* TO 'factory_user'@'%';
FLUSH PRIVILEGES;
```

#### Step 2: Start Spring Boot Backend
```bash
cd backend
# Windows:
.\mvnw.cmd spring-boot:run
# Linux / macOS:
./mvnw spring-boot:run
```
Flyway automatically applies all schema migrations `V1` through `V10` and `DataInitializer` seeds baseline master data.

#### Step 3: Start React Frontend
```bash
cd frontend
npm install
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) in your browser.

---

## 7. Automated Test Suite

The system includes comprehensive automated testing across 12 test suites:

```bash
cd backend
# Run all 59 backend integration tests:
.\mvnw.cmd test
```

### Test Results Summary:
```
[INFO] Results:
[INFO] Tests run: 59, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time: 23.757 s
```

| Suite | Description | Tests | Status |
|---|---|:---:|:---:|
| `FactoryCoreApplicationTests` | Context boot, Flyway migrations, actuator health | 2 | **PASS** |
| `AuthServiceIntegrationTests` | JWT token issuing, multi-office claims, account lockout | 7 | **PASS** |
| `ClientServiceIntegrationTests` | GSTIN/PAN normalization, address defaults, snapshotting | 6 | **PASS** |
| `ProductServiceIntegrationTests` | SKU immutability on PO lock, category filtering, status | 6 | **PASS** |
| `PurchaseOrderServiceIntegrationTests` | Sequential POs, state machine, approval, version diffing | 6 | **PASS** |
| `InventoryServiceIntegrationTests` | Invariants, row locking, append-only ledger, adjustments | 8 | **PASS** |
| `ShortageAndReservationIntegrationTests` | Stock reservation, deterministic FIFO allocation | 4 | **PASS** |
| `DeliveryChallanServiceIntegrationTests` | Sequential DCs, over-reservation checks, atomic dispatch | 4 | **PASS** |
| `NotificationAndAuditIntegrationTests` | Event-driven notifications, unread badges, audit logs | 5 | **PASS** |
| `DashboardAndReportIntegrationTests` | Role dashboards, outer state concealment, CSV export | 5 | **PASS** |
| `EnquiryAndDocumentIntegrationTests` | Public lead intake, 1-click conversion, document vault | 4 | **PASS** |
| `EndToEndWorkflowIntegrationTests` | Full 14-step manufacturing-to-delivery lifecycle | 2 | **PASS** |
| **TOTAL** | **12 Suites** | **59 / 59** | **100% PASS** |

### Frontend Static Verification:
```bash
cd frontend
npx tsc --noEmit
# Exits with 0 errors across all TypeScript source files
```

---

## 8. Documentation Index

Detailed engineering and operational documentation is available in the `docs/` folder:
- [System Architecture Blueprint (`docs/ARCHITECTURE.md`)](file:///d:/WORK/Project/Agrisep(jalna)/docs/ARCHITECTURE.md)
- [Implementation Plan & Phase Audit (`docs/IMPLEMENTATION_PLAN.md`)](file:///d:/WORK/Project/Agrisep(jalna)/docs/IMPLEMENTATION_PLAN.md)
- [Relational Database Schema & Invariants (`docs/DATABASE_DESIGN.md`)](file:///d:/WORK/Project/Agrisep(jalna)/docs/DATABASE_DESIGN.md)
- [Complete REST API Specification (`docs/API_SPECIFICATION.md`)](file:///d:/WORK/Project/Agrisep(jalna)/docs/API_SPECIFICATION.md)
- [Quality Assurance & Testing Strategy (`docs/TESTING_PLAN.md`)](file:///d:/WORK/Project/Agrisep(jalna)/docs/TESTING_PLAN.md)
- [Deployment & Operations Manual (`docs/DEPLOYMENT_GUIDE.md`)](file:///d:/WORK/Project/Agrisep(jalna)/docs/DEPLOYMENT_GUIDE.md)

---

## 9. License & Commercial Rights

Copyright © 2026 Indo American Equipment / Agrisep®. All rights reserved.  
Proprietary Industrial Factory Management & Inventory Management System.
