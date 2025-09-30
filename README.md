# VideoGameV3 - Microservices Architecture with Spring Boot

This project is a simplified **Steam-like** platform built using **Spring Boot**, leveraging **microservices architecture**. It features core services for managing **games**, **downloads**, and **user profiles**, all accessible via an **API Gateway**.

## 🧩 Project Structure

The application is split into the following microservices:

| Service         | Description                                                  | Port |
|-----------------|--------------------------------------------------------------|------|
| API Gateway     | Routes all requests to the appropriate microservice          | 8080 |
| Games Service   | Manages game catalog, details, and metadata                  | 8081 |
| Downloads Service | Handles game downloads, licenses, and user access rights   | 8082 |
| Profile Service | Manages user information, profiles, and authentication       | 8083 |

Each service is a standalone Spring Boot application communicating via REST APIs.

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven or Gradle
- Docker (optional, for containerization)

