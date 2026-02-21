# Exercise 00 – Welcome To Servlets

## Overview

A Java Servlet API web application for a movie theater booking system, implementing user registration and authentication. No Spring MVC — Spring is used purely as a DI container and utility library.

## Stack

- **Jakarta Servlet API** on Apache Tomcat 11
- **Spring Core** — IoC/DI container, `JdbcTemplate`
- **Spring Security Crypto** — BCrypt password hashing
- **HikariCP** — JDBC connection pooling
- **PostgreSQL** — relational database
- **Docker / Docker Compose** — containerization

---

## How It Works

### Spring as a DI Container (no Spring MVC)

Spring is configured via a plain Java class — no `web.xml`, no `DispatcherServlet`. All beans (DataSource, JdbcTemplate, PasswordEncoder, services, repositories) are declared in `ApplicationConfig`:
```java
@Configuration
@ComponentScan(basePackages = "fr._42.cinema")
@PropertySource("classpath:application.properties")
public class ApplicationConfig {

    @Bean
    public DataSource hikariDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(env.getProperty("db.url"));
        // ...
        return new HikariDataSource(config);
    }

    @Bean
    public PasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Bridging Spring and the Servlet Container

Since there is no Spring MVC, the Spring context must be bootstrapped manually. `ContextLoaderListener` does this once at application startup and stores the service in the `ServletContext` so every servlet can reach it:
```java
@WebListener
public class ContextLoaderListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        ApplicationContext ctx =
            new AnnotationConfigApplicationContext(ApplicationConfig.class);
        UsersService usersService = ctx.getBean(UsersService.class);
        sce.getServletContext().setAttribute("usersService", usersService);
    }
}
```

Servlets then retrieve the service from the context:
```java
UsersService usersService =
    (UsersService) getServletContext().getAttribute("usersService");
```

---

### Registration — `SignUpServlet`

On `GET /signUp`, the servlet forwards to the HTML registration form. On `POST /signUp`, it reads the form fields, delegates to the service, and redirects to the login page:
```java
public void doPost(HttpServletRequest req, HttpServletResponse res) {
    String firstName = req.getParameter("firstName");
    // ... other fields
    usersService.signUp(firstName, lastName, phone, password);
    res.sendRedirect("/signIn");
}
```

The service hashes the password with BCrypt before persisting:
```java
public void signUp(String firstName, String lastName, String phone, String password) {
    String hash = passwordEncoder.encode(password);
    usersRepository.save(new User(0L, firstName, lastName, phone, hash));
}
```

---

### Authentication — `SignInServlet`

On `POST /signIn`, the servlet reads phone and password, asks the service to authenticate, and either creates a session or redirects back:
```java
public void doPost(HttpServletRequest req, HttpServletResponse res) {
    User user = usersService.signIn(phone, password);

    if (user == null) {
        res.sendRedirect("/signUp");
        return;
    }

    HttpSession session = req.getSession();
    session.setAttribute("user", user);
    res.sendRedirect("/profile");
}
```

The service looks up the user by phone and verifies the password with BCrypt:
```java
public User signIn(String phone, String password) {
    Optional<User> userOpt = usersRepository.findByPhone(phone);
    if (userOpt.isEmpty()) return null;

    User user = userOpt.get();
    return passwordEncoder.matches(password, user.getPassword()) ? user : null;
}
```

---

### Session Guard — `LoggingFilter`

A servlet filter intercepts all requests to `/signIn`. If a session with a valid `user` attribute already exists, the user is redirected straight to `/profile` — preventing authenticated users from seeing the login page:
```java
@WebFilter("/signIn")
public class LoggingFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user != null) {
            res.sendRedirect("/profile");
            return;
        }
        chain.doFilter(req, res);
    }
}
```

---

### Database Access — `UsersRepositoryImpl`

All SQL is executed via Spring's `JdbcTemplate`. A `RowMapper` converts result set rows into `User` objects:
```java
private RowMapper<User> mapper = (rs, rowNum) -> {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setFirstName(rs.getString("first_name"));
    // ...
    return user;
};

public Optional<User> findByPhone(String phone) {
    try {
        User res = jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE phone = ?", mapper, phone);
        return Optional.of(res);
    } catch (EmptyResultDataAccessException e) {
        return Optional.empty();
    }
}
```

---

## Project Structure
```
Cinema/
├── src/main/java/fr/_42/cinema/
│   ├── config/          # ApplicationConfig — Spring Java Config
│   ├── filters/         # LoggingFilter — session guard
│   ├── listeners/       # ContextLoaderListener — Spring bootstrap
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

## Endpoints

| URL | Method | Description |
|---|---|---|
| `/signUp` | GET | Registration form |
| `/signUp` | POST | Submit registration |
| `/signIn` | GET | Login form |
| `/signIn` | POST | Submit credentials |
| `/profile` | GET | Blank profile page (authenticated users only) |

---

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

> The server container intentionally waits 10 seconds before starting Tomcat to give PostgreSQL time to finish initializing and execute the SQL init scripts.