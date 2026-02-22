# Java Servlet API — Complete Guide

A comprehensive guide covering everything you need to understand and build Java web applications using the Servlet API.

---

## Table of Contents

1. [What Is a Servlet?](#1-what-is-a-servlet)
2. [The Servlet Container](#2-the-servlet-container)
3. [Servlet Lifecycle](#3-servlet-lifecycle)
4. [Core Servlet Classes](#4-core-servlet-classes)
5. [HttpServletRequest](#5-httpservletrequest)
6. [HttpServletResponse](#6-httpservletresponse)
7. [ServletContext](#7-servletcontext)
8. [Servlet Annotations](#8-servlet-annotations)
9. [Listeners](#9-listeners)
10. [Filters](#10-filters)
11. [Dispatcher Types](#11-dispatcher-types)
12. [RequestDispatcher](#12-requestdispatcher)
13. [Session Management](#13-session-management)
14. [Graceful Shutdown](#14-graceful-shutdown)
15. [File Upload](#15-file-upload)

---

## 1. What Is a Servlet?

### The Problem

Before servlets, web applications were built with:

- **Static HTML** — cannot handle logins, databases, or dynamic content
- **CGI scripts** — dynamic but extremely slow (new process created per request)

Neither approach could handle:
- User authentication
- Database queries
- Form processing
- Session state

### The Solution

A **Servlet** is a Java class that runs inside a web server and handles HTTP requests dynamically.

Servlets solve all of these problems:

| Problem | Servlet Solution |
|---|---|
| Static pages | Generates dynamic HTML at runtime |
| Heavy CGI processes | Runs once, stays in memory, handles threads |
| No session state | Built-in `HttpSession` management |
| No database access | Java code — use any JDBC/ORM |
```java
public class HelloServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");
        response.getWriter().println("<h1>Hello, " +
            request.getParameter("name") + "!</h1>");
    }
}
```

---

## 2. The Servlet Container

### What It Is

A **Servlet Container** (also called a web container) is the program that runs your servlets. You never write socket or threading code — the container handles all of that.

Popular containers:
- **Apache Tomcat** (most common)
- **Eclipse Jetty**
- **WildFly**

### What the Container Does For You

**1. HTTP Handling**
```
Browser → TCP Connection → Container parses HTTP → Creates Java objects
```

**2. URL-to-Servlet Mapping**

The container builds an internal mapping table at startup:
```
"/hello"    → HelloServlet instance
"/api/*"    → ApiServlet instance
"*.jsp"     → JspServlet instance
```

Mapping types in order of priority:

| Type | Pattern Example | Description |
|---|---|---|
| Exact match | `/login` | Only matches this exact URL |
| Path match | `/api/*` | Matches any URL under `/api/` |
| Extension match | `*.jsp` | Matches any URL ending in `.jsp` |
| Default | `/` | Catches everything else |

**3. Request/Response Object Creation**

For every request the container creates:
```java
HttpServletRequest req  = new RequestImpl(...);  // wraps raw HTTP
HttpServletResponse res = new ResponseImpl(...); // wraps output stream
```

**4. Thread Management**

The container creates (or reuses) a thread per request:
```java
// Conceptually, for each request:
Thread t = new Thread(() -> servlet.service(request, response));
t.start();
```

**5. Other Responsibilities**

- Session management (generates `JSESSIONID`)
- Application isolation (each app has its own classloader and `ServletContext`)
- Servlet lifecycle management

### Web Application Structure
```
myapp/
├── WEB-INF/
│   ├── web.xml          ← deployment descriptor
│   ├── classes/         ← compiled .class files
│   └── lib/             ← dependency JARs
├── index.html
└── styles.css
```

The **context path** is the application name in the URL:
```
http://localhost:8080/myapp/login
                      ↑      ↑
               context path  servlet path
```

---

## 3. Servlet Lifecycle

Every servlet follows this exact lifecycle, managed entirely by the container:
```
                 ┌─────────────────┐
                 │   Class Loading  │  ← Tomcat loads the .class file
                 └────────┬────────┘
                          ↓
                 ┌─────────────────┐
                 │  Instantiation  │  ← new HelloServlet() — ONCE ONLY
                 └────────┬────────┘
                          ↓
                 ┌─────────────────┐
                 │     init()      │  ← called once, for setup
                 └────────┬────────┘
                          ↓
         ┌────────────────────────────────┐
         │         service()              │  ← called per request
         │                               │
         │  GET  → doGet()               │
         │  POST → doPost()              │
         │  PUT  → doPut()               │
         └────────────────────────────────┘
                          ↓
                 ┌─────────────────┐
                 │   destroy()     │  ← called once, on shutdown
                 └─────────────────┘
```

### Single Instance, Multiple Threads

⚠️ This is critical to understand:
```java
// Tomcat creates ONE object total:
HelloServlet servlet = new HelloServlet();

// But many threads call it simultaneously:
// Thread 1 (User A) → servlet.service(req1, res1)
// Thread 2 (User B) → servlet.service(req2, res2)
// Thread 3 (User C) → servlet.service(req3, res3)
```

This means **servlets must be thread-safe**. Never store request-specific data in instance fields:
```java
// ❌ WRONG — shared between all users
public class BadServlet extends HttpServlet {
    private String username; // race condition!

    protected void doGet(...) {
        username = request.getParameter("user");
    }
}

// ✅ CORRECT — local variables are thread-safe
public class GoodServlet extends HttpServlet {
    protected void doGet(...) {
        String username = request.getParameter("user"); // local = safe
    }
}
```

### init() and destroy()
```java
public class DatabaseServlet extends HttpServlet {

    private ConnectionPool pool;

    @Override
    public void init() throws ServletException {
        // runs once at startup — safe to initialize shared resources
        pool = new ConnectionPool();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // use pool here
    }

    @Override
    public void destroy() {
        // runs once at shutdown — close shared resources
        pool.shutdown();
    }
}
```

---

## 4. Core Servlet Classes

### Inheritance Chain
```
Object
  └── GenericServlet          ← protocol-independent base
        └── HttpServlet       ← HTTP-specific, what you extend
              └── YourServlet ← your code
```

### `GenericServlet`

Protocol-independent base class. Provides:
```java
getServletContext()   // access application-wide state
getServletConfig()    // access init parameters
log("message")        // logging
```

### `HttpServlet`

What you always extend. Provides HTTP-specific methods:
```java
protected void doGet(HttpServletRequest req, HttpServletResponse res)  {}
protected void doPost(HttpServletRequest req, HttpServletResponse res) {}
protected void doPut(HttpServletRequest req, HttpServletResponse res)  {}
protected void doDelete(HttpServletRequest req, HttpServletResponse res){}
```

The base `service()` method routes to the correct `doXxx()` based on HTTP method:
```java
// Inside HttpServlet (simplified):
protected void service(HttpServletRequest req, HttpServletResponse res) {
    String method = req.getMethod();
    if (method.equals("GET"))       doGet(req, res);
    else if (method.equals("POST")) doPost(req, res);
    // ...
}
```

### `ServletConfig`

Per-servlet configuration, injected by the container during `init()`:
```java
public void init(ServletConfig config) {
    String timeout = config.getInitParameter("timeout");
    ServletContext ctx = config.getServletContext();
}
```

---

## 5. HttpServletRequest

Represents the full HTTP request sent by the browser. The container parses the raw HTTP text and gives you a Java object.
```
POST /login HTTP/1.1
Host: localhost:8080
Cookie: JSESSIONID=abc123

username=john&password=secret
         ↓
HttpServletRequest object
```

### What You Can Get From It
```java
// HTTP method
String method = request.getMethod();         // "GET", "POST"

// URL information
String uri  = request.getRequestURI();       // /myapp/login
String ctx  = request.getContextPath();      // /myapp
String path = request.getServletPath();      // /login

// Query parameters (/search?q=java&page=2)
String query = request.getParameter("q");    // "java"
String page  = request.getParameter("page"); // "2"

// Form data (POST body) — Tomcat parses it for you
String user  = request.getParameter("username");
String pass  = request.getParameter("password");

// Headers
String agent  = request.getHeader("User-Agent");
String cookie = request.getHeader("Cookie");

// Client info
String ip = request.getRemoteAddr();         // client IP address

// Session
HttpSession session = request.getSession();

// Raw body (for JSON etc.)
InputStream body = request.getInputStream();
```

---

## 6. HttpServletResponse

Represents what you send back to the browser. You control everything.
```java
// Status code
response.setStatus(200);
response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404

// Headers
response.setHeader("X-Custom-Header", "value");
response.setContentType("text/html;charset=UTF-8");
response.setContentLengthLong(file.length());

// Write HTML body
PrintWriter out = response.getWriter();
out.println("<h1>Hello World</h1>");

// Write binary body (images, files)
OutputStream out = response.getOutputStream();
out.write(bytes);

// Redirect browser to another URL
response.sendRedirect("/profile");

// Send error
response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
```

### Forward vs Redirect

| | `forward()` | `sendRedirect()` |
|---|---|---|
| Happens on | Server side | Client side (new request) |
| URL changes? | No | Yes |
| Speed | Faster | Slower (extra round trip) |
| Data sharing | Via request attributes | Via session |

---

## 7. ServletContext

### One Per Web Application

There is exactly **one** `ServletContext` per deployed web application, shared by all servlets:
```
Tomcat
 ├── /app1  → ServletContext_app1  (shared by ALL servlets in app1)
 └── /app2  → ServletContext_app2  (shared by ALL servlets in app2)
```

### How Servlets Get Access To It

The container injects it during `init()` via `ServletConfig`. `GenericServlet` stores it and exposes `getServletContext()`:
```java
// Simplified internals of GenericServlet:
public ServletContext getServletContext() {
    return config.getServletContext(); // returns the shared context object
}
```

### What You Can Do With It

**1. Share data between servlets**
```java
// In Servlet A (or a listener at startup):
getServletContext().setAttribute("dbPool", pool);

// In Servlet B:
ConnectionPool pool =
    (ConnectionPool) getServletContext().getAttribute("dbPool");
```

**2. Read application-wide config (from web.xml)**
```xml
<!-- web.xml -->
<context-param>
    <param-name>appVersion</param-name>
    <param-value>1.0</param-value>
</context-param>
```
```java
String version = getServletContext().getInitParameter("appVersion");
```

**3. Access files on disk**
```java
String realPath = getServletContext().getRealPath("/WEB-INF/config.txt");
```

**4. Detect MIME types**
```java
String mime = getServletContext().getMimeType("photo.png"); // "image/png"
```

### Init Parameter vs Context Parameter

| | Servlet Init Parameter | Context Parameter |
|---|---|---|
| Scope | One specific servlet | Entire application |
| Access | `getServletConfig().getInitParameter()` | `getServletContext().getInitParameter()` |
| Use case | Servlet-specific config | Global config (DB URL, version) |

### Thread Safety Warning

`ServletContext` is accessed by all threads simultaneously:
```java
// ⚠️ Not thread-safe if multiple threads modify it
context.setAttribute("counter", count + 1);

// ✅ Use thread-safe data structures
context.setAttribute("map", new ConcurrentHashMap<>());
```

---

## 8. Servlet Annotations

### `@WebServlet`

Maps a servlet to a URL without needing `web.xml`:
```java
@WebServlet("/report")
public class ReportServlet extends HttpServlet { }
```

With multiple attributes:
```java
@WebServlet(
    urlPatterns  = { "/report", "/reports" },
    loadOnStartup = 1   // initialize at startup, not on first request
)
public class ReportServlet extends HttpServlet { }
```

`loadOnStartup` value:
- **Negative** (default) → initialized on first request
- **0 or positive** → initialized at startup. Lower number = initialized first.

### `@WebInitParam`

Pass configuration values to a specific servlet:
```java
@WebServlet(
    urlPatterns = "/report",
    initParams  = {
        @WebInitParam(name = "author", value = "John"),
        @WebInitParam(name = "version", value = "2")
    }
)
public class ReportServlet extends HttpServlet {

    private String author;

    @Override
    public void init() {
        author = getServletConfig().getInitParameter("author"); // "John"
    }
}
```

### `UnavailableException`

Throw from `init()` when startup fails:
```java
@Override
public void init() throws ServletException {
    if (databaseNotAvailable()) {
        throw new UnavailableException("Database not available");
    }
}
```

The container will not route any requests to this servlet.

---

## 9. Listeners

### What They Are

Listeners implement the **Observer pattern** — they react automatically when container-level events happen, without you needing to add logic to every servlet.

### The 3 Scopes and Their Events
```
Application Scope (ServletContext)  — one per app
    ├── contextInitialized / contextDestroyed
    └── attributeAdded / attributeRemoved / attributeReplaced

Session Scope (HttpSession)         — one per user
    ├── sessionCreated / sessionDestroyed
    └── attributeAdded / attributeRemoved / attributeReplaced

Request Scope (ServletRequest)      — one per HTTP request
    ├── requestInitialized / requestDestroyed
    └── attributeAdded / attributeRemoved / attributeReplaced
```

### `ServletContextListener` — Application Startup/Shutdown

The most commonly used listener. Perfect for initializing shared resources:
```java
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // App is starting — initialize shared resources
        ConnectionPool pool = new ConnectionPool();
        sce.getServletContext().setAttribute("dbPool", pool);
        System.out.println("Application started, pool ready.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // App is shutting down — clean up
        ConnectionPool pool =
            (ConnectionPool) sce.getServletContext().getAttribute("dbPool");
        pool.shutdown();
    }
}
```

### `HttpSessionListener` — Track Active Users
```java
@WebListener
public class SessionTracker implements HttpSessionListener {

    private static final AtomicInteger activeUsers = new AtomicInteger(0);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("Session created: " + se.getSession().getId());
        activeUsers.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("Session destroyed");
        activeUsers.decrementAndGet();
    }

    public static int getActiveUsers() {
        return activeUsers.get();
    }
}
```

### `ServletContextAttributeListener` — React to Context Changes
```java
@WebListener
public class ContextAttributeLogger implements ServletContextAttributeListener {

    public void attributeAdded(ServletContextAttributeEvent event) {
        System.out.println("Added to context: " + event.getName());
    }

    public void attributeRemoved(ServletContextAttributeEvent event) {
        System.out.println("Removed from context: " + event.getName());
    }

    public void attributeReplaced(ServletContextAttributeEvent event) {
        System.out.println("Replaced in context: " + event.getName());
    }
}
```

### `ServletRequestListener` — Per-Request Lifecycle
```java
@WebListener
public class RequestTimer implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute("startTime",
            System.currentTimeMillis());
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        long start = (Long) sre.getServletRequest().getAttribute("startTime");
        long duration = System.currentTimeMillis() - start;
        System.out.println("Request took " + duration + "ms");
    }
}
```

### `HttpSessionBindingListener` — Object Notified When Bound to Session

Your model object can listen to its own session binding:
```java
public class User implements HttpSessionBindingListener {

    public void valueBound(HttpSessionBindingEvent event) {
        System.out.println("User " + this.name + " logged in.");
    }

    public void valueUnbound(HttpSessionBindingEvent event) {
        System.out.println("User " + this.name + " logged out.");
    }
}
```

---

## 10. Filters

### What They Are

A filter sits **between the client and your servlet**. It can inspect, modify, or block requests and responses.
```
Browser
   ↓
Filter 1 (e.g. logging)
   ↓
Filter 2 (e.g. authentication)
   ↓
Servlet
   ↓
Filter 2 (response phase)
   ↓
Filter 1 (response phase)
   ↓
Browser
```

### Why Filters Exist

Without filters, you duplicate cross-cutting logic in every servlet:
```java
// ❌ Without filters — duplicated in 20 servlets
public void doGet(...) {
    if (!isLoggedIn(request)) { response.sendRedirect("/login"); return; }
    log(request);
    // actual business logic
}
```
```java
// ✅ With filters — written once, applied everywhere
@WebFilter("/*")
public class AuthFilter implements Filter { ... }
```

### The Filter Interface
```java
public interface Filter {
    void init(FilterConfig filterConfig) throws ServletException;
    void doFilter(ServletRequest request, ServletResponse response,
                  FilterChain chain) throws IOException, ServletException;
    void destroy();
}
```

### `chain.doFilter()` — The Key Method
```java
// ✅ Let the request continue to the next filter or servlet
chain.doFilter(request, response);

// 🚫 Block the request (don't call chain.doFilter)
response.sendError(HttpServletResponse.SC_FORBIDDEN);
return;
```

### Example 1: Logging Filter
```java
@WebFilter("/*")
public class LoggingFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println("→ " + req.getMethod() + " " + req.getRequestURI());

        long start = System.currentTimeMillis();
        chain.doFilter(request, response); // continue
        long duration = System.currentTimeMillis() - start;

        System.out.println("← Completed in " + duration + "ms");
    }

    public void init(FilterConfig fc) {}
    public void destroy() {}
}
```

### Example 2: Authentication Filter (Blocking)
```java
@WebFilter("/profile")
public class AuthFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Login required");
            return; // ← request blocked, servlet never called
        }

        chain.doFilter(request, response); // ← user is logged in, continue
    }

    public void init(FilterConfig fc) {}
    public void destroy() {}
}
```

### Example 3: Adding Security Headers to Responses
```java
@WebFilter("/*")
public class SecurityHeaderFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request, response); // let servlet run first

        // then modify the response
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
    }

    public void init(FilterConfig fc) {}
    public void destroy() {}
}
```

### Request/Response Wrappers

When you need to modify request or response data, wrap them:
```java
// Wrapper that uppercases a specific parameter
public class UpperCaseRequestWrapper extends HttpServletRequestWrapper {

    public UpperCaseRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return (value != null) ? value.toUpperCase() : null;
    }
}

// In the filter:
chain.doFilter(new UpperCaseRequestWrapper(req), response);
```

---

## 11. Dispatcher Types

### The Problem

Not all requests come directly from the browser. Some are internal:
```
Browser → /login (REQUEST)
             ↓
        LoginServlet → forward → /dashboard (FORWARD)
```

Dispatcher types tell a filter **when** it should run.

### The 5 Types

| Type | When It Triggers |
|---|---|
| `REQUEST` | Direct client request (default) |
| `FORWARD` | `requestDispatcher.forward()` |
| `INCLUDE` | `requestDispatcher.include()` |
| `ERROR` | Error page dispatch (404, 500 handler) |
| `ASYNC` | Async processing resumes |

### Default Behavior

If you don't specify, filters only run on `REQUEST`:
```java
@WebFilter("/*") // implicitly: dispatcherTypes = {DispatcherType.REQUEST}
public class MyFilter implements Filter { ... }
```

### Specifying Dispatcher Types
```java
@WebFilter(
    urlPatterns = "/*",
    dispatcherTypes = {
        DispatcherType.REQUEST,
        DispatcherType.FORWARD
    }
)
public class MyFilter implements Filter { ... }
```

### Practical Example
```
Browser → LoginServlet → forward() → DashboardServlet

Filter with REQUEST only:
  ✔ Runs for LoginServlet
  ✗ Does NOT run for DashboardServlet

Filter with REQUEST + FORWARD:
  ✔ Runs for LoginServlet
  ✔ Runs for DashboardServlet
```

**Real-world rule of thumb:**
- Authentication filters → `REQUEST` only (don't re-authenticate internal forwards)
- Logging filters → `REQUEST` + `FORWARD` + `ERROR` (log everything)
- Error page filters → `ERROR`

---

## 12. RequestDispatcher

### What It Is

`RequestDispatcher` lets one servlet invoke another resource **server-side**, invisibly to the browser.

### Getting a Dispatcher
```java
// Relative path (from request)
RequestDispatcher rd = request.getRequestDispatcher("/dashboard.jsp");

// Absolute path (from context)
RequestDispatcher rd = getServletContext().getRequestDispatcher("/dashboard.jsp");
```

### `include()` — Insert Content

Runs another resource and inserts its output into the current response. Control returns to the caller.
```java
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        res.getWriter().println("<html><body>");

        // Include header from another servlet or JSP
        req.getRequestDispatcher("/header.jsp").include(req, res);

        res.getWriter().println("<p>Main content here</p>");
        res.getWriter().println("</body></html>");
    }
}
```

### `forward()` — Transfer Control

Hands off to another resource completely. The current servlet stops. The browser URL does **not** change.
```java
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String user = req.getParameter("username");

        if (authenticates(user)) {
            req.setAttribute("message", "Welcome, " + user + "!");
            req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp")
               .forward(req, res); // ← transfers control to JSP
        } else {
            res.sendRedirect("/login?error=1"); // ← tells browser to go elsewhere
        }
    }
}
```
```jsp
<%-- dashboard.jsp --%>
<h2>${message}</h2>
```

### MVC Pattern

This is the standard **Model-View-Controller** pattern in servlet applications:
```
Browser
  ↓ POST /login
Controller (LoginServlet)       ← processes logic
  ↓ forward()
View (dashboard.jsp)            ← renders HTML
  ↓
Browser
```

### Critical Rule

You cannot write to the response body **before** calling `forward()`:
```java
// ❌ IllegalStateException — response already started
res.getWriter().println("Hello");
rd.forward(req, res);

// ✅ Set attributes, then forward
req.setAttribute("data", value);
rd.forward(req, res);
```

---

## 13. Session Management

### The Problem: HTTP Is Stateless

Every HTTP request is independent. The server has no built-in memory of previous requests:
```
Request 1: POST /cart/add    → adds item
Request 2: GET  /cart        → which user's cart? Server doesn't know!
```

### `HttpSession` — The Solution

The container creates a unique session object per user and links it to subsequent requests via a cookie:
```
Server creates session → sends JSESSIONID=abc123 cookie
Browser sends that cookie with every request
Server finds session by cookie → restores user state
```

### Creating and Accessing Sessions
```java
// Get existing session or create new one
HttpSession session = request.getSession();

// Get existing session only (returns null if none exists)
HttpSession session = request.getSession(false);
```

### Storing and Reading Data
```java
// Store
session.setAttribute("user", userObject);
session.setAttribute("cart", cartList);

// Read
User user = (User) session.getAttribute("user");

// Remove
session.removeAttribute("cart");
```

### Session Timeout

Sessions expire after inactivity. Default is typically 30 minutes.
```java
// Read timeout (in seconds)
int seconds = session.getMaxInactiveInterval();

// Set timeout
session.setMaxInactiveInterval(600); // 10 minutes

// Configure globally in web.xml (in minutes)
// <session-config>
//     <session-timeout>30</session-timeout>
// </session-config>
```

### Login / Logout Pattern
```java
// LOGIN — after validating credentials
HttpSession session = request.getSession();
session.setAttribute("user", authenticatedUser);
response.sendRedirect("/profile");

// LOGOUT
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate(); // destroys session and all its data
        }
        res.sendRedirect("/login");
    }
}
```

### Shopping Cart Example
```java
// Add to cart
@WebServlet("/cart/add")
public class AddToCartServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session = req.getSession();

        @SuppressWarnings("unchecked")
        List<String> cart =
            (List<String>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        cart.add(req.getParameter("item"));
        res.sendRedirect("/cart");
    }
}

// View cart
@WebServlet("/cart")
public class ViewCartServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        HttpSession session = req.getSession(false);
        if (session == null) {
            res.sendRedirect("/login");
            return;
        }

        List<String> cart = (List<String>) session.getAttribute("cart");
        // render cart...
    }
}
```

---

## 14. Graceful Shutdown

### The Problem

When the server shuts down, `destroy()` is called — but long-running requests may still be active in other threads:
```
Thread 1 (processing 5-minute upload) ──────────────────────►
Thread 2 (normal request)             ────►
destroy() called ───────────────────────────────────────────►
                                       ↑ problem: Thread 1 still running!
```

If you immediately close resources in `destroy()`, Thread 1 will crash.

### The Solution: Track Active Requests
```java
public class SafeServlet extends HttpServlet {

    private int activeRequests = 0;
    private boolean shuttingDown = false;

    // Synchronized counter methods
    private synchronized void incrementCounter() { activeRequests++; }
    private synchronized void decrementCounter() { activeRequests--; }
    private synchronized int  getCount()         { return activeRequests; }
    private synchronized void setShuttingDown()  { shuttingDown = true; }
    private synchronized boolean isShuttingDown(){ return shuttingDown; }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        incrementCounter();
        try {
            // Check if we should still accept work
            if (isShuttingDown()) {
                res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
            super.service(req, res); // routes to doGet/doPost
        } finally {
            decrementCounter(); // always decrement, even on exception
        }
    }

    @Override
    public void destroy() {
        if (getCount() > 0) {
            setShuttingDown(true);
        }

        // Wait for all active requests to finish
        while (getCount() > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Now safe to release resources
        closeDatabase();
    }
}
```

### Why `synchronized`?

`activeRequests` is accessed from multiple threads simultaneously. Without `synchronized`, two threads could read-modify-write at the same time, corrupting the count.

---

## 15. File Upload

### The Problem Before Servlet 3.0

When a browser uploads a file, it sends `multipart/form-data`:
```
------Boundary
Content-Disposition: form-data; name="avatar"; filename="photo.png"
Content-Type: image/png

(binary image data)
------Boundary--
```

Servlet's normal `request.getParameter()` cannot parse this format. Before Servlet 3.0, you needed external libraries like Apache Commons FileUpload.

### Servlet 3.0 Solution: Built-in Multipart Support

Add `@MultipartConfig` to your servlet and the container handles parsing automatically.

### `@MultipartConfig` Attributes Explained
```java
@MultipartConfig(
    location          = "/tmp",           // temp dir for files exceeding threshold
    fileSizeThreshold = 1024 * 1024,      // 1MB: below → memory, above → disk
    maxFileSize       = 1024 * 1024 * 5,  // 5MB max per file
    maxRequestSize    = 1024 * 1024 * 25  // 25MB max total request size
)
```

| Attribute | Effect |
|---|---|
| `location` | Where temp files go when they exceed threshold |
| `fileSizeThreshold` | Below this → kept in memory; above → written to temp disk |
| `maxFileSize` | Per-file limit. Exceeded → `IllegalStateException` |
| `maxRequestSize` | Total request limit. Exceeded → `IllegalStateException` |

### The `Part` Interface

Each section of a multipart request is a `Part`:
```java
Part filePart = request.getPart("avatar");

filePart.getName();                // form field name: "avatar"
filePart.getSubmittedFileName();   // original name: "photo.png"
filePart.getContentType();         // MIME type: "image/png"
filePart.getSize();                // size in bytes

filePart.write("/uploads/photo.png");  // save to disk
filePart.getInputStream();             // read raw bytes
filePart.delete();                     // delete temp file
```

### Example 1: Single File Upload

**HTML:**
```html
<form action="/upload" method="post" enctype="multipart/form-data">
    <input type="file" name="avatar" required>
    <button type="submit">Upload</button>
</form>
```

**Servlet:**
```java
@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,
    maxFileSize       = 1024 * 1024 * 5,
    maxRequestSize    = 1024 * 1024 * 10
)
public class UploadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Part filePart = req.getPart("avatar");

        if (filePart == null || filePart.getSize() == 0) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file uploaded.");
            return;
        }

        String originalName = filePart.getSubmittedFileName();
        String extension    = originalName.substring(originalName.lastIndexOf("."));

        // Generate unique filename to prevent collisions
        String savedName = UUID.randomUUID().toString() + extension;

        filePart.write("/var/uploads/" + savedName);

        res.getWriter().println("Uploaded as: " + savedName);
    }
}
```

### Example 2: Validate It's a Real Image

Use `ImageIO.read()` to verify the file is actually an image:
```java
InputStream stream  = filePart.getInputStream();
BufferedImage image = ImageIO.read(stream);

if (image == null) {
    res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  "Uploaded file is not a valid image.");
    return;
}
// Safe to save
```

### Example 3: File + Text Fields Together
```html
<form action="/register" method="post" enctype="multipart/form-data">
    <input type="text" name="username">
    <input type="file" name="avatar">
    <button>Register</button>
</form>
```
```java
// Text fields still work normally
String username = request.getParameter("username");

// File part accessed separately
Part avatar = request.getPart("avatar");
```

### Example 4: Multiple Files
```html
<input type="file" name="photos" multiple>
```
```java
Collection<Part> parts = request.getParts();

for (Part part : parts) {
    // Skip text fields — they have no submitted filename
    if (part.getSubmittedFileName() == null ||
        part.getSubmittedFileName().isEmpty()) {
        continue;
    }

    String fileName = UUID.randomUUID() + "_" + part.getSubmittedFileName();
    part.write("/var/uploads/" + fileName);
}
```

### Memory vs Disk — What Actually Happens
```
File size < fileSizeThreshold  →  stored in JVM heap memory
File size > fileSizeThreshold  →  written to location directory as temp file
                                  → call part.write() to move to permanent location
                                  → container deletes temp file after request ends
```

### Handling Size Limit Errors
```java
try {
    Part filePart = request.getPart("avatar"); // may throw if limits exceeded
    filePart.write("/uploads/" + fileName);
} catch (IllegalStateException e) {
    // maxFileSize or maxRequestSize exceeded
    res.sendError(HttpServletResponse.SC_BAD_REQUEST, "File too large.");
}
```

---

## Quick Reference
```
Request comes in
      ↓
Container parses HTTP
      ↓
Filters run (in order)
      ↓
Servlet.service() called
      ↓
doGet() / doPost() runs
      ↓
Response sent
      ↓
Filters run (response phase, reverse order)
      ↓
Browser receives response
```
```
Scope        Object          Lifetime
─────────────────────────────────────────────
Application  ServletContext  App start → stop
Session      HttpSession     Login    → logout/timeout
Request      HttpServletReq  Request  → response sent
```