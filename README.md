# GSIS- Leave Management System (LMS)

A web-based Leave Management System developed with Jakarta EE, JSF, PrimeFaces, JPA, and MySQL.

The application allows employees to submit leave requests and managers to review, approve, or reject them through a secure enterprise-style environment.

---

# Technology Stack

## Backend
- Jakarta EE
- CDI (Contexts and Dependency Injection)
- JPA (Jakarta Persistence API)
- EclipseLink / Hibernate
- Payara Server

## Frontend
- Jakarta Server Faces (JSF)
- PrimeFaces

## Database
- MySQL

## Security
- BCrypt password hashing
- Role-Based Access Control (RBAC)

---

# Features

## Employee Features
- User registration and login
- Secure password hashing with BCrypt
- Leave request submission
- Leave balance tracking
- Leave history view
- Validation for overlapping leave requests
- Working day calculation excluding weekends

## Manager Features
- View pending leave requests
- Approve or reject requests
- Add manager comments
- View leave statistics and charts
- Audit log tracking

---

# Database Schema

The system uses the following main tables:

- `roles`
- `employees`
- `leaves`
- `audit_logs`

---

# Project Structure

```text
src/main/java/com/company/lms
│
├── config/
├── controller/
├── model/
├── repository/
├── service/
└── util/

src/main/webapp
│
├── employee/
├── manager/
├── WEB-INF/
└── resources/
````

---

# Setup Instructions

## 1. Clone Repository

```bash
git clone <repository-url>
```

---

## 2. Configure MySQL Database

Create database:

```sql
CREATE DATABASE employee_leaves;
```

Run the provided SQL schema.

---

## 3. Configure Payara JDBC Resource

Create JDBC Resource:

```text
JNDI Name: jdbc/lms
```

Connect it to the MySQL connection pool.

---

## 4. Build Project

```bash
mvn clean package
```

---

## 5. Deploy WAR to Payara

Deploy generated WAR file from:

```text
target/leave-management.war
```

---

# Roles

* `EMPLOYEE`
* `MANAGER`
