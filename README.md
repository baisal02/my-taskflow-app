# Task Management System

A Java 21 + Spring Boot 3.2.x application for managing tasks and teams with JWT authentication, PostgreSQL, and Swagger documentation.

---

## 🛠 Tech Stack
- **Java 21**, Spring Boot 3.2.x  
- **PostgreSQL 16**, Maven  
- **JWT Auth**, Refresh Tokens  
- **Swagger/OpenAPI**, Docker, docker-compose  
- **Testing:** JUnit, Mockito, H2  

---

## 🚀 Run Locally
Clone the project into your machine 
```bash
# 1. Build
./mvnw clean package

# 2. Run
java -jar target/taskflow-0.0.1-SNAPSHOT.jar
# or
./mvnw spring-boot:run
```
Note: Port 8080 must be free for the app container
Swagger UI → [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)  

---

## 🐳 Run with Docker Compose (App + DB)
```bash
./mvnw clean package
docker compose up --build
```
Note: Port 8080 must be free for the app container
Swagger UI → [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)  

**Admin Login:**  
`admin` / `admin`  

---

## 📂 Structure

src/main/java        → Application code
src/main/resources   → Config files
src/test/java        → Tests
Dockerfile           → Docker build
docker-compose.yml   → App + DB setup




