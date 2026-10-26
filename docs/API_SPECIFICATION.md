# FACTORY CORE — REST API SPECIFICATION
**Indo American Equipment / Agrisep®**  
*Factory Management & Inventory Management System*

---

## 1. Global API Conventions

- **Base URL**: `/api/v1`
- **Transport**: HTTPS (HTTP allowed in local dev)
- **Content-Type**: `application/json; charset=utf-8`
- **Authentication**: Bearer JWT (`Authorization: Bearer <token>`) or HttpOnly Cookie `refresh_token`
- **Correlation ID**: Every request is tagged with an `X-Correlation-ID` header (or auto-generated UUID) reflected in all logs, audit records, and error responses.

---

## 2. Standard Envelopes

### 2.1 Success Response (`ApiResponse<T>`)
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-10-26T18:30:00.000Z",
  "correlationId": "8f9a2b5e-4c31-419a-a352-7104bce3fa88"
}
```

### 2.2 Paginated Response (`PageResponse<T>`)
```json
{
  "success": true,
  "message": "Results retrieved",
  "data": {
    "items": [ ... ],
    "page": 0,
    "size": 20,
    "totalItems": 150,
    "totalPages": 8,
    "isFirst": true,
    "isLast": false
  },
  "timestamp": "2026-10-26T18:30:00.000Z",
  "correlationId": "8f9a2b5e-4c31-419a-a352-7104bce3fa88"
}
```

### 2.3 Error Response (`ErrorResponse`)
```json
{
  "timestamp": "2026-10-26T18:30:00.000Z",
  "status": 409,
  "code": "INSUFFICIENT_STOCK",
  "message": "Insufficient available stock for product AGR-SC-5000.",
  "fieldErrors": [
    {
      "field": "quantity",
      "message": "Requested quantity 5 exceeds available balance 2."
    }
  ],
  "correlationId": "8f9a2b5e-4c31-419a-a352-7104bce3fa88"
}
```

---

## 3. Endpoint Catalog Summary

### 3.1 Authentication & Profile (`/api/v1/auth`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Public | Authenticate with email/password; returns JWT + sets refresh cookie |
| `POST` | `/api/v1/auth/refresh` | Public | Rotate refresh token and issue new access token |
| `POST` | `/api/v1/auth/logout` | Authenticated | Revoke refresh token and invalidate session |
| `GET` | `/api/v1/auth/me` | Authenticated | Return currently logged-in user profile, role, and office permissions |

### 3.2 Users & Organization (`/api/v1/users`, `/api/v1/offices`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/users` | `OWNER`, `ADMIN` | List users with search, role filter, pagination |
| `POST` | `/api/v1/users` | `OWNER`, `ADMIN` | Create internal user account |
| `PUT` | `/api/v1/users/{id}` | `OWNER`, `ADMIN` | Update user details, role (requires Owner approval), or office |
| `PATCH` | `/api/v1/users/{id}/status`| `OWNER` | Activate or deactivate user |
| `GET` | `/api/v1/offices` | Authenticated | List all operational offices and godowns |
| `POST` | `/api/v1/offices` | `ADMIN`, `OWNER` | Create branch office or godown |

### 3.3 Clients (`/api/v1/clients`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/clients` | Authenticated | List clients (paginated, filtered by search/state/status) |
| `GET` | `/api/v1/clients/{id}` | Authenticated | Get full client details and delivery addresses |
| `POST` | `/api/v1/clients` | `MAIN_OFFICE`, `ADMIN`, `OWNER` | Create new client with normalized GSTIN verification |
| `PUT` | `/api/v1/clients/{id}` | `MAIN_OFFICE`, `ADMIN`, `OWNER` | Update client details |
| `POST` | `/api/v1/clients/{id}/addresses` | `MAIN_OFFICE`, `ADMIN`, `OWNER` | Add delivery site address |
| `GET` | `/api/v1/clients/{id}/orders` | Authenticated | Historical POs placed by this client |

### 3.4 Products (`/api/v1/products`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/products` | Authenticated | List products with specifications |
| `GET` | `/api/v1/products/public`| Public | Catalog for public website (`website_visibility = PUBLIC`) |
| `POST` | `/api/v1/products` | `ADMIN`, `OWNER`, `MAIN_OFFICE` | Create machinery product |
| `PUT` | `/api/v1/products/{id}` | `ADMIN`, `OWNER`, `MAIN_OFFICE` | Update product (SKU immutable if referenced in any PO) |
| `PATCH` | `/api/v1/products/{id}/status` | `ADMIN`, `OWNER` | Activate / Inactivate product |

### 3.5 Purchase Orders (`/api/v1/purchase-orders`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/purchase-orders` | Authenticated (Office Scoped) | List POs with status, client, date filters |
| `GET` | `/api/v1/purchase-orders/{id}` | Authenticated (Office Scoped) | Get PO details, item list, snapshots, version info |
| `POST` | `/api/v1/purchase-orders` | `MAIN_OFFICE`, `OUTER_STATE_OFFICE`, `ADMIN`, `OWNER` | Create draft purchase order |
| `PUT` | `/api/v1/purchase-orders/{id}` | Submitter / Office | Edit PO (triggers versioning if already approved) |
| `POST` | `/api/v1/purchase-orders/{id}/submit` | Submitter / Office | Submit draft for Owner approval |
| `POST` | `/api/v1/purchase-orders/{id}/approve` | `OWNER` | Owner approves PO; triggers stock reservation |
| `POST` | `/api/v1/purchase-orders/{id}/return` | `OWNER` | Owner returns PO to draft with reason |
| `POST` | `/api/v1/purchase-orders/{id}/cancel-request` | Submitter / Office | Request PO cancellation |
| `POST` | `/api/v1/purchase-orders/{id}/cancel-approve` | `OWNER` | Owner approves cancellation; releases reserved stock |
| `GET` | `/api/v1/purchase-orders/{id}/versions` | Authenticated | Get version history and diffs |

### 3.6 Inventory & Stock Ledger (`/api/v1/inventory`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/inventory` | `OWNER`, `ADMIN`, `MAIN_OFFICE`, `GODOWN_MANAGER` | View godown stock balances (`OUTER_STATE_OFFICE` blocked) |
| `POST` | `/api/v1/inventory/inward` | `GODOWN_MANAGER`, `OWNER` | Inward stock arrival; triggers shortage auto-allocation |
| `POST` | `/api/v1/inventory/adjust` | `OWNER`, `GODOWN_MANAGER` | Manual stock adjustment with mandatory audit remark |
| `GET` | `/api/v1/inventory/ledger` | `OWNER`, `ADMIN`, `GODOWN_MANAGER` | View append-only stock movement ledger |
| `GET` | `/api/v1/inventory/shortages` | `OWNER`, `ADMIN`, `MAIN_OFFICE`, `GODOWN_MANAGER` | View open/partial stock shortages sorted by FIFO priority |

### 3.7 Delivery Challans & Dispatch (`/api/v1/delivery-challans`, `/api/v1/dispatch`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/delivery-challans` | Authenticated (Office Scoped) | List DCs |
| `GET` | `/api/v1/delivery-challans/{id}` | Authenticated (Office Scoped) | DC details, vehicle info, dispatch quantities |
| `POST` | `/api/v1/delivery-challans` | `GODOWN_MANAGER`, `OWNER` | Create DC for reserved PO items |
| `POST` | `/api/v1/delivery-challans/{id}/approve` | `OWNER`, Delegated `ADMIN` | Approve DC for physical dispatch |
| `POST` | `/api/v1/dispatch/{dcId}/execute` | `GODOWN_MANAGER`, `OWNER` | Execute physical dispatch; decrements `on_hand` and `reserved` |
| `POST` | `/api/v1/deliveries/confirm` | `GODOWN_MANAGER`, `OWNER` | Confirm customer delivery with delivery date & proof |

### 3.8 In-App Notifications (`/api/v1/notifications`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/notifications` | Authenticated | List paginated notifications (optional `?unreadOnly=true`) |
| `GET` | `/api/v1/notifications/unread-count` | Authenticated | Get real-time unread notification badge count |
| `PATCH` | `/api/v1/notifications/{id}/read` | Authenticated | Mark a single notification as read |
| `PATCH` | `/api/v1/notifications/mark-all-read` | Authenticated | Mark all notifications for the user as read |

### 3.9 Centralized Audit Trail (`/api/v1/audit`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/audit/logs` | `OWNER`, `ADMIN` | Search tamper-evident audit logs by `action`, `entityName`, `userId`, `startDate`, `endDate` |
| `GET` | `/api/v1/audit/logs/entity/{entityName}/{entityId}` | `OWNER`, `ADMIN` | Retrieve chronological audit history for a specific business entity |

### 3.10 Role-Tailored Operational Dashboards (`/api/v1/dashboard`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/dashboard/owner` | `OWNER` | Executive KPIs: Total revenue, pending approvals, open shortages, inventory valuation, recent POs |
| `GET` | `/api/v1/dashboard/admin` | `ADMIN`, `OWNER` | System health, user counts, office metrics, system activity logs |
| `GET` | `/api/v1/dashboard/main-office` | `MAIN_OFFICE`, `OWNER` | Office sales, PO progress, dispatch schedules, client counts |
| `GET` | `/api/v1/dashboard/outer-state` | `OUTER_STATE_OFFICE` | State-scoped POs and shipments (strictly hides physical warehouse balances) |
| `GET` | `/api/v1/dashboard/godown` | `GODOWN_MANAGER`, `OWNER` | Godown inventory balances, pending dispatches, incoming goods |
| `GET` | `/api/v1/dashboard/summary` | Authenticated | Generic multi-role summary card data |

### 3.11 Multi-Format Business Reports (`/api/v1/reports`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `GET` | `/api/v1/reports/inventory-valuation` | `OWNER`, `ADMIN`, `GODOWN_MANAGER` | JSON inventory valuation with reorder alerts |
| `GET` | `/api/v1/reports/inventory-valuation/export` | `OWNER`, `ADMIN`, `GODOWN_MANAGER` | Stream CSV export of inventory valuation and reorder levels |
| `GET` | `/api/v1/reports/po-fulfillment` | `OWNER`, `ADMIN`, `MAIN_OFFICE` | JSON purchase order fulfillment and delivery metrics |
| `GET` | `/api/v1/reports/po-fulfillment/export` | `OWNER`, `ADMIN`, `MAIN_OFFICE` | Stream CSV export of PO lifecycle and fulfillment rates |
| `GET` | `/api/v1/reports/dispatches` | `OWNER`, `ADMIN`, `GODOWN_MANAGER`, `MAIN_OFFICE` | JSON dispatch tracking with transporter and vehicle info |
| `GET` | `/api/v1/reports/dispatches/export` | `OWNER`, `ADMIN`, `GODOWN_MANAGER`, `MAIN_OFFICE` | Stream CSV export of dispatches and delivery confirmations |

### 3.12 Website Leads & Enquiries (`/api/v1/public/enquiries`, `/api/v1/enquiries`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/public/enquiries` | Public (Unauthenticated) | Submit machinery enquiry / lead from public website |
| `GET` | `/api/v1/enquiries` | `MAIN_OFFICE`, `ADMIN`, `OWNER` | List and search enquiries with filters for status and search query |
| `GET` | `/api/v1/enquiries/{id}` | `MAIN_OFFICE`, `ADMIN`, `OWNER` | Get full enquiry lead details |
| `PATCH` | `/api/v1/enquiries/{id}/status` | `MAIN_OFFICE`, `ADMIN`, `OWNER` | Update enquiry triage status (`NEW`, `CONTACTED`, `QUALIFIED`, `CLOSED`) |
| `POST` | `/api/v1/enquiries/{id}/convert-to-client` | `MAIN_OFFICE`, `ADMIN`, `OWNER` | 1-Click lead conversion to verified enterprise client |

### 3.13 Secure Document Storage Vault (`/api/v1/documents`)
| Method | Path | Required Role | Description |
|---|---|---|---|
| `POST` | `/api/v1/documents/upload` | Authenticated | Upload attachment with SHA-256 integrity hash and security validation |
| `GET` | `/api/v1/documents/{id}/metadata` | Authenticated | Get document metadata (filename, size, MIME type, entity link) |
| `GET` | `/api/v1/documents/{id}/download` | Authenticated | Stream file download with path traversal protection |
| `DELETE` | `/api/v1/documents/{id}` | `ADMIN`, `OWNER` | Delete document record and purge file |
