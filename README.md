# GSIS Leave Management System

A Jakarta EE web application for managing employee leave requests.

Employees can register, sign in, submit leave requests, and review their leave history. Managers can review pending requests, approve or reject them with comments, view leave statistics, and inspect the audit log.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Database Setup](#database-setup)
- [Build and Deployment](#build-and-deployment)
- [Application Routes](#application-routes)
- [Domain Model](#domain-model)
- [Security Notes](#security-notes)
- [Development Notes](#development-notes)

## Features

### Employee

- **Registration and login**: Employees can create an account and authenticate with email and password.
- **Password validation**: New passwords must contain at least 8 characters, one uppercase letter, one lowercase letter, and one number.
- **Leave request submission**: Employees can submit leave requests with date range, leave type, and reason.
- **Leave balance checks**: Requests are validated against the employee's available annual leave balance.
- **Leave history**: Employees can view their submitted requests and their statuses.
- **Overlap prevention**: The system blocks overlapping pending or approved leave requests.
- **Working-day calculation**: Leave duration excludes weekends and configured Greek public holidays.

### Manager

- **Pending request review**: Managers can view all pending leave requests.
- **Approval workflow**: Approved requests deduct working days from the employee leave balance.
- **Rejection workflow**: Rejected requests require a manager comment.
- **Audit trail**: Approvals and rejections are logged.
- **Statistics dashboard**: Managers can view charts grouped by leave status and leave type.

## Tech Stack

- **Language**: Java 21
- **Backend platform**: Jakarta EE 10
- **Web framework**: Jakarta Server Faces with Facelets
- **UI components**: PrimeFaces 13 Jakarta classifier
- **Dependency injection**: Jakarta CDI
- **Persistence**: Jakarta Persistence API with JTA
- **Database**: MySQL
- **Application server**: Payara Server 7 compatible runtime
- **Security utility**: jBCrypt
- **Build tool**: Maven
- **Packaging**: WAR

## Architecture

The application follows a layered Jakarta EE structure:

```text
JSF pages
  -> Controllers
  -> Services
  -> Repositories
  -> JPA entities
  -> MySQL
```

- **Controllers** handle JSF view state and user actions.
- **Services** contain business rules, transactions, leave validation, balance updates, statistics, authentication, and audit logging.
- **Repositories** encapsulate JPA queries and persistence operations.
- **Entities** map the relational model used by JPA.
- **Utilities** provide password hashing and Greek public holiday calculation.

## Project Structure

```text
.
├── pom.xml
├── README.md
├── src
│   └── main
│       ├── java
│       │   └── com/company/lms
│       │       ├── controller
│       │       ├── model
│       │       ├── repository
│       │       ├── service
│       │       └── util
│       ├── resources
│       │   └── META-INF/persistence.xml
│       └── webapp
│           ├── employee
│           ├── manager
│           ├── resources
│           │   ├── css
│           │   └── images
│           ├── WEB-INF
│           │   ├── template
│           │   └── web.xml
│           ├── login.xhtml
│           └── register.xhtml
└── WEB-INF
```

The deployable web application descriptor is `src/main/webapp/WEB-INF/web.xml`.

## Prerequisites

- **Java**: JDK 21
- **Maven**: Maven 3.8+
- **Database**: MySQL 8+
- **Application server**: Payara Server 7 (Jakarta EE compatible)

## Configuration

### Persistence unit

The JPA persistence unit is defined in:

```text
src/main/resources/META-INF/persistence.xml
```

Important values:

- **Persistence unit**: `lmsPU`
- **Transaction type**: `JTA`
- **JNDI data source**: `jdbc/lms`
- **Schema generation**: `jakarta.persistence.schema-generation.database.action=create`

### Web configuration

The active JSF configuration is:

```text
src/main/webapp/WEB-INF/web.xml
```

Important values:

- **Faces servlet mapping**: `*.xhtml`
- **Welcome page**: `login.xhtml`
- **Session timeout**: 30 minutes
- **JSF project stage**: `Development`

For production deployments, change `jakarta.faces.PROJECT_STAGE` from `Development` to `Production`.

## Database Setup

Create the application database:

```sql
CREATE DATABASE employee_leaves CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Create a Payara JDBC connection pool for MySQL and expose it through this JNDI resource:

```text
jdbc/lms
```

The application expects the following JPA-managed tables:

- `roles`
- `employees`
- `leaves`
- `audit_logs`

Before users can register successfully, the `roles` table must contain the employee role:

```sql
INSERT INTO roles (role_name) VALUES ('EMPLOYEE');
```

Manager access requires a user whose role is `MANAGER`:

```sql
INSERT INTO roles (role_name) VALUES ('MANAGER');
```

User registration creates `EMPLOYEE` accounts. Manager accounts must be created or assigned at the database level unless an administration feature is added.

## Build and Deployment

Build the WAR:

```bash
mvn clean package
```

The generated artifact is:

```text
target/leave-management.war
```

Deploy the WAR to Payara after configuring the `jdbc/lms` JNDI resource.

When deployed with the configured context root, the application is available at:

```text
http://localhost:8080/leave-management-system
```

## Application Routes

- **Login**: `/login.xhtml`
- **Register**: `/register.xhtml`
- **Employee dashboard**: `/employee/dashboard.xhtml`
- **Employee leave history**: `/employee/history.xhtml`
- **Manager requests**: `/manager/requests.xhtml`
- **Manager statistics**: `/manager/stats.xhtml`
- **Manager audit log**: `/manager/audit.xhtml`

## Domain Model

- **Role**: Defines application roles such as `EMPLOYEE` and `MANAGER`.
- **Employee**: Stores user identity, BCrypt password hash, leave balance, and role.
- **LeaveRequest**: Stores requested date range, leave type, reason, manager comment, and status.
- **AuditLog**: Stores manager actions against leave requests.

Leave request statuses:

- `PENDING`
- `APPROVED`
- `REJECTED`

## Security Notes

- Passwords are hashed with BCrypt using jBCrypt.
- Login state is stored in a JSF session-scoped controller.
- The UI renders employee or manager navigation based on the logged-in user's role.
- Registration always creates users with the `EMPLOYEE` role.

## Development Notes

- Business validation for leave requests is implemented in `EmployeeLeaveService`.
- Manager approval and rejection rules are implemented in `ManagerLeaveService`.
- Statistics aggregation is implemented through `StatisticsService` and `StatisticsRepository`.
- Greek public holidays are calculated in `GreekHolidayUtil`.
- No automated test suite is currently included.

## Key Learning Outcomes

- Enterprise Java architecture
- Layered application design
- JPA entity modeling
- Authentication and authorization
- Transaction management
- JSF component-based UI development
- Business rule implementation
