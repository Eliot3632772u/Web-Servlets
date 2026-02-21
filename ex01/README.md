# Exercise 00 – Welcome To Servlets

## Overview

A Java Servlet API web application prototype for a movie theater booking system, implementing user registration and authentication with a PostgreSQL database backend.

## Stack

- **Java Servlets** (Jakarta EE) on Apache Tomcat 11
- **Spring Core** (IoC/DI, JdbcTemplate) — no Spring MVC
- **HikariCP** for connection pooling
- **BCrypt** (Spring Security Crypto) for password hashing
- **PostgreSQL** as the database
- **Docker / Docker Compose** for containerization

## Architecture

The application follows a layered architecture:

**Config** → `ApplicationConfig` is a Spring `@Configuration` class that defines all beans (DataSource, JdbcTemplate, PasswordEncoder). It is bootstrapped once at startup by a `ServletContextListener` and stored in the `ServletContext` so all servlets can access it without depending on Spring MVC.

**Listener** → `ContextLoaderListener` implements `ServletContextListener`. On `contextInitialized`, it creates the Spring `AnnotationConfigApplicationContext`, retrieves the `UsersService` bean, and stores it as a servlet context attribute.

**Filter** → `LoggingFilter` intercepts GET requests to `/signIn`. If a valid session with a `user` attribute already exists, the user is redirected to `/profile`, preventing authenticated users from seeing the login page again.

**Servlets**
- `SignUpServlet` — serves the registration form on GET; on POST it validates input, calls `UsersService.signUp()` (which BCrypt-hashes the password before persisting), and redirects to `/signIn`.
- `SignInServlet` — serves the login form on GET; on POST it calls `UsersService.signIn()`, which looks up the user by phone number and verifies the password with BCrypt. On success an `HttpSession` is created with the `user` attribute and the client is redirected to `/profile`. On failure the user is redirected back to `/signUp`.
- `ProfileServlet` — blank page, accessible after a successful login.

**Service / Repository**
- `UsersServiceImpl` encapsulates business logic (hashing on sign-up, BCrypt match on sign-in).
- `UsersRepositoryImpl` uses `JdbcTemplate` for all SQL operations and exposes `findByPhone` for authentication lookups.

## Project Structure
```
Cinema/
├── src/main/java/fr/_42/cinema/
│   ├── config/          # Spring Java Config (ApplicationConfig)
│   ├── filters/         # LoggingFilter
│   ├── listeners/       # ContextLoaderListener
│   ├── models/          # User POJO
│   ├── repositories/    # CrudRepository, UsersRepository, UsersRepositoryImpl
│   ├── services/        # UsersService, UsersServiceImpl
│   └── servlets/        # SignUpServlet, SignInServlet, ProfileServlet
└── src/main/resources/
    ├── application.properties   # DB connection config
    └── sql/
        ├── schema.sql           # Table definitions
        └── data.sql             # Optional seed data
```

## Running the Application

**Start:**
```bash
docker compose up --build
```

**Stop and clean up:**
```bash
docker compose down --volumes --remove-orphans
```

The app will be available at `http://localhost:8080`.

> The server container waits 10 seconds before starting Tomcat to give PostgreSQL time to initialize and run the SQL init scripts.

## Endpoints

| URL | Method | Description |
|---|---|---|
| `/signUp` | GET | Registration form |
| `/signUp` | POST | Submit registration |
| `/signIn` | GET | Login form |
| `/signIn` | POST | Submit login |
| `/profile` | GET | Blank profile page (post-login) |