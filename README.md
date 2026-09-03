# GEDA IIoT SCADA SaaS — Backend (Demo Slice)

Spring Boot 3.3 / Java 17 backend for the GEDA IIoT SCADA SaaS demo slice: Login + OTP/MFA,
Main Dashboard, Org Hierarchy, and Device Management.

## Prerequisites

- **Java 17** (JDK)
- **Maven 3.9+**
- **Local MySQL 8** running as a Windows service (no Docker required for the backend)
  - Create a database named `geda` and a user `geda` with a password, matching the values in
    the repo-root [`.env.example`](../.env.example). Copy that file to `.env` at the repo root
    (`d:\projects\GEDA\.env`) and adjust credentials if your local MySQL setup differs:

    ```
    MYSQL_HOST=localhost
    MYSQL_PORT=3306
    MYSQL_DB=geda
    MYSQL_USER=geda
    MYSQL_PASSWORD=geda
    ```

  - Example one-time setup in the MySQL shell (adjust as needed):

    ```sql
    CREATE DATABASE geda CHARACTER SET utf8mb4;
    CREATE USER 'geda'@'localhost' IDENTIFIED BY 'geda';
    GRANT ALL PRIVILEGES ON geda.* TO 'geda'@'localhost';
    FLUSH PRIVILEGES;
    ```

  - The application reads `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DB` / `MYSQL_USER` / `MYSQL_PASSWORD`
    as environment variables (with `localhost:3306/geda/geda/geda` as defaults), so exporting the
    `.env` values into your shell environment before running is optional but recommended for
    matching non-default credentials.

- No Redis is required. OTP codes are stored in-memory (`InMemoryOtpStore`), which is sufficient
  for a single-instance local demo.

## How to run

From the `backend/` folder:

```bash
mvn spring-boot:run
```

This runs with the `dev` Spring profile active by default (configured in `application.yml`), which:
- Enables `app.otp.dev-expose=true` so login responses include a `devOtpCode` field for demo convenience.
- Allows CORS from `http://localhost:5173` (the Vite frontend dev server).
- Enables more verbose `com.qpaix.geda` and `org.hibernate.SQL` logging.

On startup, Flyway applies `V1__init_schema.sql` (schema) and `V2__seed_data.sql` (seed data:
roles, org tree, seeded admin user, ~15 devices, 24h of generation readings, ~10 alerts) against
the `geda` database.

The API is served under the `/api` context path, e.g. `http://localhost:8080/api/auth/login`.

## Seeded admin login

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `Admin@123` |
| Role | `SUPER_ADMIN` |
| MFA required | Yes |

Because this user has `mfa_required=true`, `POST /api/auth/login` will return a `pendingToken`
instead of an access token. The OTP code needed to complete login via
`POST /api/auth/otp/verify` is:

- **Always logged** to the backend console (`log.info` in `OtpService`), e.g.:
  `Generated OTP for userId=1: 482913`
- **Also returned in the API response** as `devOtpCode` on the `/api/auth/login` response body,
  since the `dev` profile sets `app.otp.dev-expose=true`. This field is omitted entirely when
  `app.otp.dev-expose=false` (the production-safe default in `application.yml`).

## Swagger UI

With the app running, interactive API docs are available at:

```
http://localhost:8080/api/swagger-ui.html
```

(OpenAPI JSON at `http://localhost:8080/api/v3/api-docs`.)

## Notes

- `spring.jpa.hibernate.ddl-auto=validate` — Flyway owns the schema; Hibernate only validates
  entity mappings against it at startup.
- Redis is intentionally not used for this slice. OTP storage is abstracted behind the
  `OtpStore` interface (`com.qpaix.geda.auth.service`) so a Redis-backed implementation can be
  swapped in later without touching `AuthService`/`OtpService` call sites.
- The root `docker-compose.yml` (MySQL + Redis) is left in place from an earlier plan revision
  but is unused by this backend, which connects to a locally installed MySQL instance instead.
