# Testing (kxreport)

## Run tests

```bash
mvn test
```

## What is covered

### HTTP smoke tests

- `ReportControllerJsonTest`
  - Verifies `GET /json` responds `200 OK` and returns an array of JSON objects.
  - Uses a real report definition from `report/A00.jrxml`.
    - The test compiles it at runtime into a temporary `A00.jasper` and points `kxreport.report.path` to that temp folder.
  - Verifies the endpoint is resilient to invalid `.jasper` files (it should still respond `200 OK`).

### Unit tests (fast, no Spring context)

- `ParameterTest`
  - Ensures request parameter setters convert blank strings to `null`.

- `ReportPathResolverTest`
  - Ensures report path resolution prefers `/<root>/<app>/<db>/<report>.jasper` when present.
  - Falls back to `/<root>/<app>/<report>.jasper`.
  - Defaults report name to `A00` when `report` is `null`.

## Implementation notes

- Tests were enabled in `pom.xml` (previously `maven.test.skip` was `true`).
- `spring-boot-starter-test` was added for JUnit5 + Spring test utilities.
- Report path resolution was extracted from `ReportController` into `ReportPathResolver` to make it unit-testable.

## What is intentionally NOT tested (for now)

### Database-backed report generation

- Endpoints like `POST /openPDF`, `POST /filePDF`, `POST /fileCSV` require:
  - A reachable MariaDB database with expected schema/data, and
  - A `.jasper` report that runs successfully against that schema.

These tests are intentionally excluded to keep the test suite:
- Fast
- Deterministic
- Not dependent on local/CI database credentials

If you later want DB integration tests, the recommended approach is to use a disposable MariaDB instance (e.g., Testcontainers) instead of hardcoding DB user/password.
