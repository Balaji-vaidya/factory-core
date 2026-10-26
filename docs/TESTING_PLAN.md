# FACTORY CORE — TESTING PLAN & QUALITY ASSURANCE STRATEGY
**Indo American Equipment / Agrisep®**  
*Factory Management & Inventory Management System*

---

## 1. Testing Philosophy & Quality Gates

Testing in Factory Core is a non-negotiable verification gate. Because this system controls physical factory inventory, capital machinery, legal delivery challans, and customer dispatch, software failures directly cause operational stoppages or stock discrepancies.

### Quality Criteria:
1. Zero regression on mathematical inventory invariants ($ON\_HAND \ge 0$, $RESERVED \le ON\_HAND$).
2. 100% test coverage for state transition rules (PO, DC, Shortages).
3. Concurrency guarantees: Multi-threaded test executions for stock reservations and sequential number generation.
4. Security tests: Role-based authorization and office/state scoping tested on every protected API endpoint.

---

## 2. Testing Pyramid & Framework Matrix

| Level | Toolchain | Target Scope |
|---|---|---|
| **Backend Unit Tests** | JUnit 5, Mockito, AssertJ | Domain entities, PO state machines, version diff calculations, DTO validations, inventory formula evaluators. |
| **Backend Integration Tests**| Spring Boot Test, MockMvc, MySQL / Testcontainers | Controller-service-repository integration, database constraints, Flyway migration validity, Spring Security filters, transaction rollbacks. |
| **Concurrency Tests** | Java `CompletableFuture` / `ExecutorService`, JUnit 5 | Concurrent stock reservations, race conditions on stock inward, sequential PO/DC number collision avoidance. |
| **Frontend Unit & Component**| Vitest, React Testing Library | Form validations (Zod schemas), component rendering, role-based navigation guards, badge status formats. |
| **End-to-End Workflow** | Spring Boot Integration E2E / Playwright | Full operational cycle from Client Creation to Delivery Confirmation. |

---

## 3. Critical Business Test Suites

### 3.1 Suite 1: Inventory Concurrency & Invariant Enforcement
- `test_reserveStock_success_whenSufficientAvailable()`
- `test_reserveStock_createsShortage_whenInsufficientAvailable()`
- `test_reserveStock_fails_whenConcurrentRequestsExceedStock()`: 10 concurrent threads attempt to reserve 5 units each from an on-hand balance of 10. Exactly 2 threads must succeed, 8 threads must either receive shortages or conflict errors; final `reserved` must not exceed 10.
- `test_negativeStock_rejectedByCheckConstraint()`: Attempt to force `on_hand = -1` triggers database constraint violation.

### 3.2 Suite 2: Purchase Order Lifecycle & Versioning
- `test_draftPO_doesNotTouchInventory()`: Creating or modifying a draft PO has zero effect on `inventory` or `stock_ledger`.
- `test_ownerApprove_triggersStockReservation()`: Owner approval transitions status to `APPROVED` and invokes inventory reservation.
- `test_editApprovedPO_releasesReservations_andCreatesVersion()`: Editing an already-approved PO immediately cancels existing reservations, creates a `POVersion` with field diffs, and resets status to `DRAFT` requiring re-approval.
- `test_duplicateProductInSamePO_rejected()`: Attempting to insert the same `product_id` twice in a single PO throws a validation error.

### 3.3 Suite 3: Deterministic FIFO Shortage Allocation
- `test_shortageAllocation_prioritizesPoDate_thenExpectedDeliveryDate()`:
  - Setup: PO-001 created on Oct 10 (needs 5 machines). PO-002 created on Oct 12 (needs 5 machines).
  - Both POs have open shortages because inventory was 0.
  - Stock arrival of 6 machines occurs.
  - Assert: PO-001 receives all 5 required machines (shortage resolved). PO-002 receives the remaining 1 machine (shortage partially resolved, 4 remaining). Total reserved = 6, available = 0.

### 3.4 Suite 4: Delivery Challan & Dispatch Validation
- `test_createDC_fails_whenQuantityExceedsReserved()`: Creating a DC for 10 units when only 6 are reserved throws `409 Conflict`.
- `test_dispatch_decrementsBothOnHandAndReserved()`:
  - Initial: On Hand = 10, Reserved = 10, Available = 0.
  - Dispatch quantity = 4.
  - Result: On Hand = 6, Reserved = 6, Available = 0. Ledger entry `DC_DISPATCH` recorded with delta -4.
- `test_duplicateDispatch_rejectedByIdempotency()`: Replaying the same dispatch request is safely rejected without double-decrementing stock.

### 3.5 Suite 5: Security & Scope Isolation
- `test_outerStateOffice_cannotViewGodownStockBalances()`: Calling `GET /api/v1/inventory` with an `OUTER_STATE_OFFICE` token returns `403 Forbidden`.
- `test_outerStateOffice_cannotAccessOtherStatePOs()`: Calling `GET /api/v1/purchase-orders/{id}` for a PO belonging to another office returns `403 Forbidden`.
- `test_adminCannotApprovePO_withoutOwnerRole()`: Calling `POST /api/v1/purchase-orders/{id}/approve` with `ADMIN` role returns `403 Forbidden`.

---

## 4. End-to-End (E2E) Master Test Scenario

The master E2E test verifies the 14-step factory workflow in a single integration suite:
1. **Bootstrap**: Authenticate as default Owner.
2. **Setup User**: Create `MAIN_OFFICE` and `GODOWN_MANAGER` users.
3. **Master Data**: Create Client (Aakash Seeds, GSTIN: `27AABCA1234F1Z9`) and Machinery Product (`AGR-SC-5000`).
4. **Initial Stock**: Godown Manager logs initial stock of 5 units of `AGR-SC-5000`.
5. **PO Creation**: Main Office user drafts PO for 8 units of `AGR-SC-5000`.
6. **Submission**: Main Office submits PO for approval.
7. **Approval & Partial Reservation**: Owner approves PO:
   - 5 units reserved.
   - 3 units recorded as an open `StockShortage`.
   - PO status moves to `NOT_DELIVERED`, reservation status `PARTIALLY_RESERVED`.
8. **Restocking**: Godown Manager inward arrivals 10 units of `AGR-SC-5000`.
9. **FIFO Allocation**: System auto-allocates 3 units to the open shortage; shortage status becomes `RESOLVED`; reserved quantity becomes 8.
10. **Delivery Challan**: Godown Manager creates DC for partial shipment of 4 units (`DC-000001`).
11. **Dispatch**: Owner approves DC; Godown Manager executes dispatch.
    - Physical inventory drops by 4; reserved drops by 4.
    - PO status becomes `PARTIALLY_DELIVERED`.
12. **Second DC & Dispatch**: DC created and dispatched for remaining 4 units.
13. **Delivery Confirmation**: Godown Manager records customer receipt confirmation with date and POD.
    - PO status moves to `DELIVERED`.
14. **Audit Verification**: Assert all 13 preceding actions are present in `audit_logs` with proper timestamps and user identity.

---

## 5. Automated Test Verification Results (100% Pass)

All 12 test suites executed cleanly with **59/59 tests passing** across the complete enterprise stack:

| Test Suite | Class Name | Tests Passed | Status | Coverage Focus |
|---|---|:---:|:---:|---|
| **1. Application Context** | `FactoryCoreApplicationTests` | 2 | PASS | Spring Boot 3 context load, Flyway migrations V1-V10, Actuator health |
| **2. Auth & Security** | `AuthServiceIntegrationTests` | 7 | PASS | JWT token issuance, multi-office claims, account lockout (5 failed attempts), BCrypt hashing |
| **3. Client Master** | `ClientServiceIntegrationTests` | 6 | PASS | GSTIN/PAN normalization, duplicate rejection, delivery address defaults, PO client snapshot |
| **4. Product Master** | `ProductServiceIntegrationTests` | 6 | PASS | SKU immutability lock on PO binding, categories, specs, website visibility vs internal status |
| **5. Purchase Orders** | `PurchaseOrderServiceIntegrationTests` | 6 | PASS | Sequential `PO-%06d` pessimistic lock, lifecycle state machine, snapshot freezing, version diffing |
| **6. Inventory & Ledger** | `InventoryServiceIntegrationTests` | 8 | PASS | Invariant enforcement (`AVAILABLE = ON_HAND - RESERVED`), pessimistic row locking, append-only ledger |
| **7. Shortages & FIFO** | `ShortageAndReservationIntegrationTests` | 4 | PASS | Automatic reservation check, deterministic FIFO priority queue (`po_date, expected_delivery, po_number`) |
| **8. Delivery Challans** | `DeliveryChallanServiceIntegrationTests` | 4 | PASS | Sequential `DC-%06d`, unreserved stock rejection, atomic dispatch deduction, delivery completion |
| **9. Notifications & Audit** | `NotificationAndAuditIntegrationTests` | 5 | PASS | Multi-domain event logging, audit trail immutability, unread badge counter, mark-as-read |
| **10. Dashboards & Reports** | `DashboardAndReportIntegrationTests` | 5 | PASS | Owner executive KPIs, outer state warehouse concealment, live CSV export streaming |
| **11. Documents & Enquiries**| `EnquiryAndDocumentIntegrationTests` | 4 | PASS | Public lead intake, 1-click lead-to-client conversion, secure file storage & streaming |
| **12. Master End-to-End** | `EndToEndWorkflowIntegrationTests` | 2 | PASS | Complete 14-step manufacturing-to-delivery lifecycle & multi-challan partial dispatch flow |
| **TOTAL** | **12 Test Suites** | **59 / 59** | **100% PASS** | **Zero Failures, Zero Errors, Zero Skipped** |

### Frontend Static Verification:
- **TypeScript Static Typecheck**: `npx tsc --noEmit` exited with code 0 (zero errors).
- **Vite Bundler**: Clean component modularity across all feature routes and typed API services.
