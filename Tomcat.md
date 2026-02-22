# Apache Tomcat — Complete Guide

A comprehensive guide covering what Tomcat is, how it works internally, and how to use it to deploy Java Servlet applications.

---

## Table of Contents

1. [What Is Apache Tomcat?](#1-what-is-apache-tomcat)
2. [How Tomcat Works Internally](#2-how-tomcat-works-internally)
3. [Folder Structure](#3-folder-structure)
4. [Configuration](#4-configuration)
5. [Deploying Applications](#5-deploying-applications)
6. [Tomcat and Java Servlets](#6-tomcat-and-java-servlets)
7. [Thread Management](#7-thread-management)
8. [Session Management](#8-session-management)
9. [Logging](#9-logging)
10. [Security](#10-security)
11. [HTTPS Configuration](#11-https-configuration)
12. [Running Tomcat with Docker](#12-running-tomcat-with-docker)
13. [Common Issues](#13-common-issues)
14. [Resources](#14-resources)

---

## 1. What Is Apache Tomcat?

Apache Tomcat is an open-source **Servlet Container** — a program that runs Java web applications. It implements a subset of the Jakarta EE (formerly Java EE) specifications:

| Specification | What It Does |
|---|---|
| Jakarta Servlet | Handles HTTP requests with Java classes |
| Jakarta JSP | Renders dynamic HTML via Java Server Pages |
| Jakarta EL | Expression Language for JSP templates |
| WebSocket | Persistent two-way connections |

### Tomcat vs Full Application Servers

Tomcat is intentionally lightweight. It is **not** a full Jakarta EE server:

| Feature | Tomcat | WildFly / GlassFish |
|---|---|---|
| Servlets & JSP | ✅ | ✅ |
| EJB | ❌ | ✅ |
| JPA (built-in) | ❌ | ✅ |
| JMS | ❌ | ✅ |
| Weight | Light | Heavy |
| Startup time | Fast | Slow |

**Tomcat is the right choice for:**
- Servlet / JSP applications
- REST APIs
- Spring / Spring Boot applications
- MVC web apps
- Microservices

---

## 2. How Tomcat Works Internally

When Tomcat starts, it goes through these stages:
```
Tomcat starts
     ↓
Reads server.xml         ← ports, connectors, thread config
     ↓
Reads conf/web.xml       ← global default servlet config
     ↓
Scans webapps/           ← finds .war files and directories
     ↓
Deploys each application ← creates ServletContext per app
     ↓
Scans @WebServlet        ← builds URL-to-servlet mapping table
     ↓
Opens port 8080          ← starts listening for connections
     ↓
Waits for HTTP requests
```

### When a Request Arrives
```
Browser sends HTTP request
     ↓
Tomcat accepts TCP connection (Connector)
     ↓
Thread pulled from thread pool
     ↓
HTTP parsed → HttpServletRequest object created
     ↓
URL matched against mapping table
     ↓
Filters run (in order)
     ↓
Servlet.service() called → doGet() / doPost()
     ↓
HttpServletResponse written
     ↓
Thread returned to pool
```

### Tomcat Internal Components
```
Tomcat
 └── Server
      └── Service
           ├── Connector (HTTP, port 8080)
           └── Engine (Catalina)
                └── Host (localhost)
                     ├── Context (/app1)  ← one per web app
                     └── Context (/app2)
```

| Component | Role |
|---|---|
| **Connector** | Accepts TCP connections, parses HTTP |
| **Engine** | Routes requests to the correct Host |
| **Host** | Manages web applications on a virtual host |
| **Context** | Represents one deployed web application |

---

## 3. Folder Structure
```
apache-tomcat/
├── bin/         ← startup and shutdown scripts
├── conf/        ← server-wide configuration files
├── lib/         ← shared JAR libraries for all apps
├── logs/        ← server and application log files
├── temp/        ← temporary runtime files
├── webapps/     ← deployed web applications live here
└── work/        ← compiled JSP files
```

### `bin/` — Starting and Stopping
```bash
# Linux / macOS
./bin/startup.sh          # start Tomcat
./bin/shutdown.sh         # stop Tomcat
./bin/catalina.sh run     # start in foreground (useful in Docker)

# Windows
bin\startup.bat
bin\shutdown.bat
```

You can set custom environment variables in an optional file:
```bash
# bin/setenv.sh (create this file if it doesn't exist)
export JAVA_OPTS="-Xms512m -Xmx1024m"
export CATALINA_OPTS="-Denv=production"
```

### `conf/` — Configuration Files

The most important directory. Contains:

| File | Purpose |
|---|---|
| `server.xml` | Ports, connectors, threads, engine, hosts |
| `web.xml` | Global default servlet/filter config for all apps |
| `context.xml` | Default resource config applied to all apps |
| `tomcat-users.xml` | Admin users and roles |
| `logging.properties` | Log levels and log file config |

### `webapps/` — Your Applications

This is where deployed applications live:
```
webapps/
├── ROOT/           ← served at http://localhost:8080/
├── manager/        ← web-based deployment manager UI
├── host-manager/   ← virtual host manager UI
├── docs/           ← Tomcat documentation
└── myapp/          ← your application at /myapp
```

Drop a `.war` file here and Tomcat auto-deploys it.

### `work/` — Compiled JSPs

Tomcat compiles `.jsp` files into Java servlet classes and stores them here:
```
work/Catalina/localhost/myapp/
    └── org/apache/jsp/
        └── index_jsp.java    ← generated from index.jsp
        └── index_jsp.class   ← compiled bytecode
```

If JSP changes are not reflecting, delete this folder and restart.

### `logs/` — Log Files

| File | Contents |
|---|---|
| `catalina.out` | Main server log, stdout + stderr |
| `localhost.YYYY-MM-DD.log` | Per-app log messages |
| `access_log.YYYY-MM-DD.txt` | HTTP access log (all requests) |
| `catalina.YYYY-MM-DD.log` | Tomcat internal messages |

---

## 4. Configuration

### `server.xml` — The Core Configuration File
```xml
<Server port="8005" shutdown="SHUTDOWN">

  <Service name="Catalina">

    <!-- HTTP Connector — listens on port 8080 -->
    <Connector port="8080"
               protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443"
               maxThreads="200"
               minSpareThreads="10" />

    <Engine name="Catalina" defaultHost="localhost">

      <Host name="localhost" appBase="webapps"
            unpackWARs="true" autoDeploy="true">
      </Host>

    </Engine>
  </Service>
</Server>
```

Key attributes:

| Attribute | Meaning |
|---|---|
| `port="8080"` | HTTP port |
| `connectionTimeout` | How long to wait for a request (ms) |
| `maxThreads` | Max simultaneous requests |
| `minSpareThreads` | Always-ready idle threads |
| `autoDeploy="true"` | Auto-deploy new WARs dropped in webapps/ |
| `unpackWARs="true"` | Extract WAR to directory on deploy |

### `context.xml` — Application-Level Config

Applied to every application by default:
```xml
<Context>
    <!-- Session persistence across restarts -->
    <Manager pathname="" />

    <!-- Shared DataSource example -->
    <Resource name="jdbc/mydb"
              auth="Container"
              type="javax.sql.DataSource"
              username="postgres"
              password="secret"
              driverClassName="org.postgresql.Driver"
              url="jdbc:postgresql://localhost:5432/mydb" />
</Context>
```

### `tomcat-users.xml` — Admin Access

Required to use the Manager web UI:
```xml
<tomcat-users>
    <role rolename="manager-gui"/>
    <role rolename="manager-script"/>
    <user username="admin"
          password="securepassword"
          roles="manager-gui,manager-script"/>
</tomcat-users>
```

Access the Manager UI at:
```
http://localhost:8080/manager/html
```

---

## 5. Deploying Applications

### Option 1: Drop WAR File (Most Common)
```bash
mvn clean package          # builds target/myapp.war
cp target/myapp.war apache-tomcat/webapps/
```

Tomcat detects the new file, extracts it, and deploys automatically. Your app becomes available at:
```
http://localhost:8080/myapp
```

### Option 2: Deploy ROOT Application

To serve your app at `http://localhost:8080/` (no context path):
```bash
# Replace the default ROOT directory
cp target/myapp.war apache-tomcat/webapps/ROOT.war
```

Or rename during Maven build in `pom.xml`:
```xml
<build>
    <finalName>ROOT</finalName>
</build>
```

### Option 3: Custom Context Path

Create a context descriptor file:
```xml
<!-- conf/Catalina/localhost/cinema.xml -->
<Context docBase="/opt/myapp" path="/cinema" reloadable="true" />
```

Your app is now at:
```
http://localhost:8080/cinema
```

`reloadable="true"` makes Tomcat watch for class changes and reload automatically (useful in development, disable in production).

### Option 4: Manager Web UI

With `tomcat-users.xml` configured, visit:
```
http://localhost:8080/manager/html
```

Upload and deploy WAR files directly from the browser.

### WAR File Structure

A WAR (Web Application Archive) is just a ZIP with a specific layout:
```
myapp.war
├── WEB-INF/
│   ├── web.xml          ← deployment descriptor (optional with annotations)
│   ├── classes/         ← compiled .class files
│   │   └── com/example/
│   │       └── MyServlet.class
│   └── lib/             ← dependency JARs
│       └── spring-core.jar
├── index.html
├── css/
└── js/
```

---

## 6. Tomcat and Java Servlets

### How Tomcat Finds Your Servlets

**Via annotation (modern approach):**
```java
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.getWriter().println("Login page");
    }
}
```

Tomcat scans all classes at startup, finds `@WebServlet`, and registers the mapping.

**Via `web.xml` (traditional approach):**
```xml
<servlet>
    <servlet-name>LoginServlet</servlet-name>
    <servlet-class>com.example.LoginServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>LoginServlet</servlet-name>
    <url-pattern>/login</url-pattern>
</servlet-mapping>
```

### What Tomcat Does With Your Servlet
```java
// Conceptually, what Tomcat does at startup:
LoginServlet instance = new LoginServlet();    // ONE instance created
instance.init(servletConfig);                 // init() called once

// Then for each request:
Thread t = new Thread(() -> {
    HttpServletRequest req  = buildRequest(rawHttp);
    HttpServletResponse res = buildResponse(socket);
    instance.service(req, res);               // routes to doGet/doPost
});
t.start();
```

### Accessing ServletContext from Your Servlet

Tomcat injects the `ServletContext` into every servlet during `init()`. All servlets in the same app share the same instance:
```java
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        // Tomcat provides this — shared across all servlets in this app
        ServletContext ctx = getServletContext();

        String appName = (String) ctx.getAttribute("appName");
        res.getWriter().println("App: " + appName);
    }
}
```

### Using a `ServletContextListener` for Initialization

The cleanest way to initialize shared resources (like Spring context, DB pools) before any servlet handles a request:
```java
@WebListener
public class AppStartupListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        // Runs once when Tomcat deploys the app
        DataSource pool = buildConnectionPool();
        sce.getServletContext().setAttribute("dataSource", pool);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // Runs once when Tomcat undeploys the app
        DataSource pool = (DataSource) sce.getServletContext()
                                          .getAttribute("dataSource");
        pool.close();
    }
}
```

---

## 7. Thread Management

Tomcat uses a thread pool to handle concurrent requests efficiently. Instead of creating a new thread per request (expensive), it keeps a pool of reusable threads.
```xml
<!-- server.xml -->
<Connector port="8080"
           maxThreads="200"       ← max simultaneous requests
           minSpareThreads="10"   ← always-warm idle threads
           acceptCount="100"      ← queue size when all threads are busy
           connectionTimeout="20000" />
```

| Setting | Effect |
|---|---|
| `maxThreads` | Hard limit on concurrent requests. Excess requests queue up |
| `minSpareThreads` | Pre-warmed threads ready to accept work immediately |
| `acceptCount` | How many requests queue when all `maxThreads` are busy. Beyond this → connection refused |

### Thread Safety Reminder

Because Tomcat calls the same servlet instance from multiple threads simultaneously, **never store request-specific data in servlet instance fields**:
```java
// ❌ DANGEROUS — instance field shared by all threads
public class BadServlet extends HttpServlet {
    private String currentUser; // race condition!
}

// ✅ SAFE — local variable is on each thread's stack
public class GoodServlet extends HttpServlet {
    protected void doGet(...) {
        String currentUser = req.getParameter("user"); // thread-local
    }
}
```

---

## 8. Session Management

Tomcat handles session management automatically. When you call `request.getSession()`, Tomcat:

1. Generates a unique session ID (`JSESSIONID`)
2. Stores the session object in memory
3. Sends the ID to the browser as a cookie
```
HTTP Response:
Set-Cookie: JSESSIONID=3F2A1B9C4D...

Subsequent requests:
Cookie: JSESSIONID=3F2A1B9C4D...  ← browser sends it back automatically
```

### Session Timeout Configuration

In your app's `WEB-INF/web.xml`:
```xml
<session-config>
    <session-timeout>30</session-timeout> <!-- minutes -->
</session-config>
```

Or globally for all apps in `conf/web.xml` (same syntax).

Or programmatically:
```java
request.getSession().setMaxInactiveInterval(1800); // 1800 seconds = 30 min
```

### Session Persistence Across Restarts

By default Tomcat serializes sessions to disk on shutdown and reloads them on startup (using `PersistentManager`). Disable this in `conf/context.xml` for clean restarts:
```xml
<Context>
    <Manager pathname="" /> <!-- empty pathname = no persistence -->
</Context>
```

---

## 9. Logging

Tomcat uses `java.util.logging` by default, configured in `conf/logging.properties`.

### Log Levels
```properties
# conf/logging.properties
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].level = INFO
org.apache.catalina.core.ContainerBase.[Catalina].[localhost].handlers = \
    2localhost.org.apache.juli.FileHandler
```

Levels in order of verbosity: `SEVERE` → `WARNING` → `INFO` → `CONFIG` → `FINE` → `FINER` → `FINEST`

### Logging From Your Servlet
```java
public class MyServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        getServletContext().log("Handling request from: " + req.getRemoteAddr());
    }
}
```

Appears in `logs/localhost.YYYY-MM-DD.log`.

### Watching Logs in Real Time
```bash
tail -f logs/catalina.out
```

---

## 10. Security

### Blocking Access to Sensitive Directories

`WEB-INF/` and `META-INF/` are **automatically protected** by Tomcat — the browser can never request files directly from these directories. Always put JSP files and config inside `WEB-INF/`:
```
WEB-INF/
├── jsp/
│   └── profile.jsp    ← not directly accessible by URL
└── web.xml
```

Serve them only via servlet forward:
```java
req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, res);
```

### Role-Based Access in `web.xml`
```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Admin Area</web-resource-name>
        <url-pattern>/admin/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>admin</role-name>
    </auth-constraint>
</security-constraint>

<login-config>
    <auth-method>FORM</auth-method>
    <form-login-config>
        <form-login-page>/login.html</form-login-page>
        <form-error-page>/login-error.html</form-error-page>
    </form-login-config>
</login-config>
```

---

## 11. HTTPS Configuration

Generate a self-signed keystore (development only):
```bash
keytool -genkey -alias tomcat -keyalg RSA \
        -keystore conf/keystore.jks \
        -keysize 2048
```

Enable HTTPS in `server.xml`:
```xml
<Connector
    port="8443"
    protocol="org.apache.coyote.http11.Http11NioProtocol"
    SSLEnabled="true"
    maxThreads="150"
    scheme="https"
    secure="true"
    keystoreFile="conf/keystore.jks"
    keystorePass="yourpassword"
    clientAuth="false"
    sslProtocol="TLS" />
```

Your app is now available at:
```
https://localhost:8443/myapp
```

To force HTTP → HTTPS redirect, add to `web.xml`:
```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>All</web-resource-name>
        <url-pattern>/*</url-pattern>
    </web-resource-collection>
    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```


---

## 14. Resources

- [Apache Tomcat Official Documentation](https://tomcat.apache.org/tomcat-11.0-doc/index.html)
- [Tomcat Configuration Reference — server.xml](https://tomcat.apache.org/tomcat-11.0-doc/config/index.html)
- [Tomcat Connector Configuration](https://tomcat.apache.org/tomcat-11.0-doc/config/http.html)