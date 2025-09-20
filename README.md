# Parking Lot System - Assignment

This is a Spring Boot project for the **Parking Lot System** coding assignment.
The solution is kept **modular, clean, and easy to explain** during the follow-up review.

---

## 🚀 Features Implemented

### Core Parking Lot Functionality

* **Domain entities**: `Vehicle`, `ParkingSlot`, `Ticket`, `Payment`, `PricingRule`
* **Layered architecture**: Controllers → Services → Repositories
* **Transactional** entry/exit flows with `@Transactional`
* **Pessimistic locking** on slot allocation to prevent double assignment
* **Nearest-slot allocation using Min Heaps**:

  * Precomputed `gate → slot → distance`
  * One heap per **Gate + VehicleType**
  * `HeapManager.rebuildHeaps()` keeps in-memory heaps in sync when Admin changes slots

### Admin Functionality

* **Slots** (`/api/admin/slots`): Add, Update, Delete

  * Duplicate prevention by `slotNumber`
  * Rebuild heaps after every change
* **Pricing Rules** (`/api/admin/pricing`): CRUD

  * DB-backed rules per `VehicleType`
  * Rule = `freeMinutes` + `ratePerHour`
  * Applied on exit: duration beyond free minutes billed per hour (rounded up)

### User Functionality

* **Entry** (`/api/user/parking/entry`): allocates nearest free slot, creates ticket
* **Exit** (`/api/user/parking/exit`): computes fee by rules, frees slot, records payment

### Security (AuthN & AuthZ)

* **Google OAuth2 Resource Server (JWT)**
  Postman obtains **ID token** from Google; APIs accept `Authorization: Bearer <id_token>`.
* **Role mapping from JWT `email` claim**:

  * Emails in `app.security.admin-emails` → `ROLE_ADMIN`
  * Others → `ROLE_USER` (or from `user-emails` list if provided)
* **Access control**

  * Admin APIs: `@PreAuthorize("hasRole('ADMIN')")`
  * User APIs: `@PreAuthorize("hasAnyRole('USER','ADMIN')")`

### Database

* **H2 in-memory** for local testing
* Seeded sample **slots** and **pricing rules**

### Quality & DX

* **Global exception handler** (consistent JSON errors)
* **Postman collection** to demo end-to-end flows

---

## ⚙️ Build & Run

**Requirements**: Java 17+, Maven

```bash
mvn clean package
mvn spring-boot:run
```

**H2 console**

* URL: `http://localhost:8080/h2-console`
* JDBC URL: `jdbc:h2:mem:parkingdb`

---

## 🔑 Authentication Flow (Postman)

1. In Postman, use **OAuth 2.0 (Authorization Code)** to get Google tokens:

   * **Auth URL**: `https://accounts.google.com/o/oauth2/v2/auth`
   * **Token URL**: `https://oauth2.googleapis.com/token`
   * **Callback URL**: `https://oauth.pstmn.io/v1/callback` (add this in GCP OAuth Client)
   * **Scope**: `openid email profile`
2. After “Get New Access Token”, **copy the `id_token`** from the token response.
3. For API requests, set **Auth Type = Bearer Token** and paste the **`id_token`**.
4. Role mapping:

   * If `email` in `app.security.admin-emails` → `ROLE_ADMIN`
   * Else → `ROLE_USER`

**YAML (excerpt)**:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://www.googleapis.com/oauth2/v3/certs

app:
  security:
    admin-emails:
      - youremail@gmail.com
    # user-emails is optional; if omitted, others become USER by default
    user-emails: []
```

---

## 📚 API Cheatsheet

**User APIs** (allowed: USER, ADMIN)

* `POST /api/user/parking/entry` → create ticket (nearest slot by gate)
* `POST /api/user/parking/exit` → compute charge & free slot

**Admin APIs** (allowed: ADMIN only)

* `GET /api/admin/slots` → list slots
* `POST /api/admin/slots` → add slot (duplicates rejected)
* `PUT /api/admin/slots/{id}` → update slot (rebuild heaps)
* `DELETE /api/admin/slots/{id}` → delete slot (rebuild heaps)
* `GET /api/admin/pricing` → list rules
* `POST /api/admin/pricing` → create/update rule for a vehicle type

---

## 🧠 What to Explain in the Review

* **Transactions**: entry & exit flows are atomic; payment + ticket + slot status in one unit.
* **Concurrency**: pessimistic locks prevent double slot allocation under high concurrency.
* **Heaps**: per Gate+VehicleType min-heaps from precomputed distances; fast nearest lookup; `rebuildHeaps()` on Admin changes.
* **Pricing**: DB-driven `PricingRule` (free minutes + rate/hour); easily extensible.
* **Security**: Google ID token validation; role mapping via JWT `email`; clean separation of Admin vs User endpoints.
* **Extensibility**: pluggable allocation and pricing strategies; easy to add gates, floors, or rules.

---

## ✅ Demo Flow (suggested)

1. **Get ID token (Admin)** in Postman → call `/api/admin/slots` & `/api/admin/pricing`.
2. **Add a slot** → verify `/api/admin/slots` → heaps auto-rebuilt.
3. **Get ID token (User)** with another Gmail → call `/api/user/parking/entry` → receive ticket.
4. **Exit** → `/api/user/parking/exit` → receipt with computed amount (rules applied).
