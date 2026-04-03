# ⬡ ServiceRegistry

A developer portal to register, manage, and monitor services and their API endpoints in real time.

---

## 📌 Features

- **Service Management** – Add, edit, delete, and search services with owner and description.
- **Endpoint Catalog** – Register API endpoints (GET/POST/PUT/DELETE) per service.
- **Real-time Health Checks** – Ping endpoints, measure response time, detect UP/DOWN status.
- **Health Logs** – View full history of health check results per endpoint.
- **Analytics Dashboard** – SQL-powered insights: total services, average response time, slowest APIs.

---

## 🛠️ Tech Stack

| Layer        | Technology                   |
|-------------|-------------------------------|
| Language     | Java (JDK 17+)               |
| UI           | Java Swing                    |
| Database     | MySQL                         |
| Connectivity | JDBC                          |
| HTTP Calls   | Java HttpURLConnection / APIs |

---

## 🗄️ Database Schema

- **services** → `id, name, owner, description, status, created_at`  
- **endpoints** → `id, service_id (FK), url, method`  
- **health_logs** → `id, endpoint_id (FK), status, status_code, response_time, checked_at`  
- **dependencies** → `id, service_id (FK), depends_on (FK)`  

---

## 🚀 Getting Started

### Prerequisites

- Java JDK 17+  
- MySQL 8.0+  
- MySQL Connector/J JAR  


### Steps

1. **Clone the repository**

```bash
git clone https://github.com/pujithabuchupalli/ServiceRegistry.git
cd ServiceRegistry
Setup the database
CREATE DATABASE service_registry;

Tables are auto-created on first run via DBConnection.initDB()

Update database credentials

Open DBConnection.java and update the following:

private static final String URL  = "jdbc:mysql://localhost:3306/service_registry";
private static final String USER = "root";
private static final String PASS = "your_password";
Compile and run
javac -cp .;mysql-connector-j.jar *.java
java -cp .;mysql-connector-j.jar Portal

On Mac/Linux, replace ; with : in classpath.
