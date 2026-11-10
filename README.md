<<<<<<< HEAD
# Inventory Management – Stage 1: Application foundation

Spring Boot 4.0.7 · Java 25 · Gradle (Kotlin DSL) · PostgreSQL 17

> **Status:** code written but NOT yet compiled or run by the author. Run the checks below on your machine.

## 1. One-time setup (Windows PowerShell)

```powershell
winget install EclipseAdoptium.Temurin.25.JDK
winget install Gradle.Gradle
# close and reopen PowerShell, then:
java -version          # must say 25
gradle -v              # must be 9.1 or newer (needed for Java 25)
```

Generate the Gradle Wrapper (creates gradlew, gradlew.bat, gradle/wrapper/*):

```powershell
cd inventory-management
gradle wrapper --gradle-version 9.2.0
```

From now on use `.\gradlew.bat` (Windows) or `./gradlew` (WSL2/Linux) instead of `gradle`.

## 2. Run the tests (no database needed)

```powershell
.\gradlew.bat test
```
Expected: `BUILD SUCCESSFUL` (16 tests: 5 service, 7 controller, 4 integration).

## 3. Start PostgreSQL, then the app

```powershell
docker run --name inventory-db -e POSTGRES_DB=inventory -e POSTGRES_USER=inventory -e POSTGRES_PASSWORD=inventory -p 5432:5432 -d postgres:17
.\gradlew.bat bootRun
```
Expected in the log: `Started InventoryManagementApplication in ... seconds`.

## 4. Try the API (second PowerShell window)

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod -Method Post http://localhost:8080/api/products -ContentType "application/json" -Body '{"name":"Gaming Laptop","description":"16GB RAM","quantity":5,"price":1299.99}'
Invoke-RestMethod http://localhost:8080/api/products
Invoke-RestMethod "http://localhost:8080/api/products?name=lap"
Invoke-RestMethod http://localhost:8080/api/products/1
Invoke-RestMethod -Method Put http://localhost:8080/api/products/1 -ContentType "application/json" -Body '{"name":"Gaming Laptop","description":"32GB RAM","quantity":3,"price":1399.99}'
Invoke-RestMethod -Method Delete http://localhost:8080/api/products/1
```

## 5. Stop and clean up

```powershell
docker rm -f inventory-db
```

---

# Stage 2: Run with Docker Compose

```powershell
cd inventory-management
docker rm -f inventory-db                    # remove the Stage 1 test database if still running
Copy-Item .env.example .env                  # then edit .env and set POSTGRES_PASSWORD
docker compose up -d --build                 # first build takes several minutes
docker compose ps                            # db should be "healthy", app "running"
Invoke-RestMethod http://localhost:8081/actuator/health
docker compose logs -f app                   # Ctrl+C to stop following
docker compose down                          # stop (data kept)   |   docker compose down -v  -> ALSO deletes data
```

Requires the Gradle Wrapper (`gradlew`, `gradle/wrapper/*`) from Stage 1, because the Dockerfile uses it.
=======
# inventory-management
>>>>>>> 61c80e945b8a90df23b23814d2d4d5e45805732f
