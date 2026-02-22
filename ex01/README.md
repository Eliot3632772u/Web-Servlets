# Exercise 01 – Authentication

## Overview

Extends [Exercise 00](../ex00/) by adding a proper authorization mechanism via servlet filters. The core application logic (registration, login, session creation) remains unchanged.

---

## Changes from Exercise 00

### `LoggingFilter` — extended to cover `/signUp`

The filter from ex00 only guarded `/signIn`. It now covers both public pages, so authenticated users are redirected to `/profile` regardless of which auth page they try to visit:
```java
@WebFilter(urlPatterns = {"/signIn", "/signUp"})
public class LoggingFilter implements Filter { ... }
```

---

### `ProfileFilter` — new filter guarding `/profile`

A second filter is added to protect the profile page. It checks for a valid session with a `user` attribute. If none is found, it returns a `403 FORBIDDEN` response instead of redirecting:
```java
@WebFilter("/profile")
public class ProfileFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user != null) {
            chain.doFilter(req, res);
            return;
        }
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Login First!");
    }
}
```

---

## Access Rules Summary

| URL | Unauthenticated | Authenticated |
|---|---|---|
| `/signUp` | ✅ Allowed | ↩ Redirect to `/profile` |
| `/signIn` | ✅ Allowed | ↩ Redirect to `/profile` |
| `/profile` | ❌ 403 Forbidden | ✅ Allowed |

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