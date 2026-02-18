1️⃣ What Is a Servlet?

A Servlet is a Java class that runs inside a web server (like:

Apache Tomcat

Jetty

)

It handles HTTP requests and generates HTTP responses.




2️⃣ What Problem Does It Solve?

Before servlets, websites were mostly:

Static HTML files

CGI scripts (very slow, heavy process creation)

🚨 The Problem

Static websites cannot:

Handle logins

Connect to databases

Process forms

Create dynamic pages

Maintain sessions

You need something that:

Receives HTTP requests

Reads parameters

Talks to database

Generates dynamic responses

Stays alive in memory (efficient)

✅ The Solution

Servlets:

Run once

Stay in memory

Handle thousands of requests using threads

Allow dynamic web applications

They solve the "dynamic server-side logic" problem.



3️⃣ Where Do Servlets Run?

Servlets run inside a Servlet Container.

Example:

Apache Tomcat

The container:

Manages servlet lifecycle

Creates servlet objects

Calls their methods

Manages threads

Handles networking

You don’t write socket code — Tomcat does that.




Step 3 — Servlet Lifecycle

Every servlet follows this lifecycle:

1️⃣ Loading

Tomcat loads the class.

2️⃣ Instantiation

Tomcat creates one instance of the servlet:

HelloServlet servlet = new HelloServlet();

⚠ Important:
Only ONE object is created (usually).

3️⃣ init()

Called once:

public void init() {
    // initialization code
}

Used for:

DB connections

Configuration

Setup

4️⃣ service()

For every request:

public void service(...)

If it's an HTTP servlet:

GET → doGet()

POST → doPost()

5️⃣ destroy()

When server shuts down:

public void destroy()




3️⃣ HttpServlet (Most Important)

From:

javax.servlet.http

This is what you normally extend:

public class HelloServlet extends HttpServlet {
}

It gives you:

doGet()
doPost()
doPut()
doDelete()




1️⃣ What is HttpServletRequest?

Class from:

jakarta.servlet.http.HttpServletRequest

It represents:

The FULL HTTP request sent by the client.

When a browser sends:

POST /login HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded
Cookie: JSESSIONID=123

username=ilyass&password=123

Tomcat parses that raw text and builds a Java object:

HttpServletRequest request

This object contains everything about the request.

What does it contain?
1️⃣ HTTP Method
request.getMethod(); // "GET" or "POST"
2️⃣ URL Information
request.getRequestURI();     // /login
request.getContextPath();    // /myapp
request.getServletPath();    // /login
3️⃣ Query Parameters

If URL is:

/search?q=java&page=2
request.getParameter("q"); // java
request.getParameter("page"); // 2
4️⃣ Form Data (POST body)
request.getParameter("username");

Tomcat already parsed the body for you.

5️⃣ Headers
request.getHeader("User-Agent");
request.getHeader("Cookie");
6️⃣ Session
HttpSession session = request.getSession();

This is how login systems work.

7️⃣ InputStream

If JSON:

request.getInputStream();




2️⃣ What is HttpServletResponse?

Represents:

The HTTP response that will be sent back to the client.

You control what goes back to the browser.

What can you control?
1️⃣ Status Code
response.setStatus(200);
response.setStatus(404);
2️⃣ Headers
response.setHeader("Content-Type", "application/json");
3️⃣ Content Type
response.setContentType("text/html");
4️⃣ Body
PrintWriter out = response.getWriter();
out.println("<h1>Hello</h1>");





3️⃣ “Tomcat Creates One Instance of the Servlet” — What Does That Mean?

This is VERY important.

Suppose you write:

public class LoginServlet extends HttpServlet {
}

When Tomcat starts:

Step 1 — Class Loading

Tomcat loads:

LoginServlet.class
Step 2 — Instantiation

Tomcat creates ONE object:

LoginServlet servlet = new LoginServlet();

ONLY ONE.

Not per request.

Step 3 — Initialization

Tomcat calls:

servlet.init();

Once.

Step 4 — Requests Come In

For each HTTP request:

Tomcat does:

Thread t = new Thread(() -> {
    servlet.service(request, response);
});
t.start();

⚠ IMPORTANT:

Multiple threads call the SAME servlet object.

Example:

User A → Thread 1 → servlet.service()

User B → Thread 2 → servlet.service()

User C → Thread 3 → servlet.service()

All using the SAME object.

Why Only One Instance?

Because:

Creating objects per request is expensive

Keeping one instance saves memory

Allows efficient threading

This is why:

⚠ Servlets must be thread-safe.





4️⃣ How Tomcat Internally Maps URLs to Servlets

Now we go deep.

Step 1 — You Define Mapping

Either in:

Old way — web.xml
<servlet>
    <servlet-name>Hello</servlet-name>
    <servlet-class>com.example.HelloServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>Hello</servlet-name>
    <url-pattern>/hello</url-pattern>
</servlet-mapping>
Modern way — Annotation
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
}
Step 2 — Tomcat Startup

When Tomcat starts:

Scans web.xml

Scans annotations

Builds internal mapping table

Internally something like:

Map<String, HttpServlet> urlMappings;

urlMappings.put("/hello", helloServletInstance);
Step 3 — HTTP Request Arrives

Browser sends:

GET /myapp/hello HTTP/1.1

Tomcat parses:

Context path: /myapp

Servlet path: /hello

Step 4 — Mapping Algorithm

Tomcat checks mapping rules in order:

Exact match

Path match (/api/*)

Extension match (*.jsp)

Default servlet

Example matching rules:

Pattern	Matches
/hello	exact
/api/*	path
*.jsp	extension
Step 5 — Found Matching Servlet

Tomcat finds:

HelloServlet instance
Step 6 — Create Request/Response Objects

Tomcat creates:

HttpServletRequest req = new RequestImpl(...);
HttpServletResponse res = new ResponseImpl(...);

These are internal implementations.

Step 7 — Call Service
servlet.service(req, res);

Inside HttpServlet:

protected void service(...) {
    if (method.equals("GET")) {
        doGet(req, res);
    }
}








1️⃣ What Are Servlet Lifecycle Events?

In a web application, many important things happen automatically:

Application starts

Application shuts down

User logs in (session created)

User logs out (session destroyed)

Request begins

Request ends

Attribute added to session

Attribute added to application context

These are called lifecycle events.

They are events generated by the servlet container (like Apache Tomcat).

2️⃣ What Problem Do Listeners Solve?

Without listeners:

You would need to manually put logging or initialization logic inside every servlet.

Example problems:

How do you initialize a database connection pool when the app starts?

How do you log when a session is created?

How do you clean up resources when the app shuts down?

How do you track active users?

You don’t want to do this inside doGet().

Listeners allow you to:

React automatically when container-level events happen.

They implement the Observer pattern.

Container = event source
Listener = event observer

3️⃣ The 3 Main Scopes in a Web App

Understanding listeners requires understanding scopes.

1️⃣ Application Scope (ServletContext)

One per web application.

Shared across ALL users.

2️⃣ Session Scope (HttpSession)

One per user.

Created when user visits.

3️⃣ Request Scope (ServletRequest)

One per HTTP request.

Created for each request.

Each of these scopes has lifecycle events.

4️⃣ ServletContextListener (Application Level)

Interface:

javax.servlet.ServletContextListener

Monitors:

Application startup

Application shutdown

Event class:

ServletContextEvent
When Is It Triggered?
On Server Startup:

Tomcat loads your app → creates ServletContext → fires:

contextInitialized()
On Shutdown:

Tomcat stops → fires:

contextDestroyed()
Example
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Application started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application stopped");
    }
}
Real Use Case

Initialize database pool:

public void contextInitialized(ServletContextEvent sce) {
    ConnectionPool pool = new ConnectionPool();
    sce.getServletContext().setAttribute("dbPool", pool);
}

Now all servlets can use it.

5️⃣ ServletContextAttributeListener

Monitors when attributes are added to application scope.

Events:

attributeAdded

attributeRemoved

attributeReplaced

Event class:

ServletContextAttributeEvent

Example:

public void attributeAdded(ServletContextAttributeEvent event) {
    System.out.println("Added: " + event.getName());
}

Triggered when:

context.setAttribute("key", value);
6️⃣ HttpSessionListener (User Session Level)

Interface:

javax.servlet.http.HttpSessionListener

Monitors:

Session creation

Session destruction

Event class:

HttpSessionEvent
When Does a Session Get Created?

When:

request.getSession();

is called.

Example
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.annotation.WebListener;

@WebListener
public class SessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("Session created: " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("Session destroyed");
    }
}
Real Use Case

Track active users:

private static int activeUsers = 0;

public void sessionCreated(HttpSessionEvent se) {
    activeUsers++;
}

public void sessionDestroyed(HttpSessionEvent se) {
    activeUsers--;
}
7️⃣ HttpSessionAttributeListener

Triggered when session attributes change:

session.setAttribute("user", userObject);

Methods:

attributeAdded

attributeRemoved

attributeReplaced

Event type:

HttpSessionBindingEvent
8️⃣ ServletRequestListener (Request Level)

Triggered:

When request starts

When request ends

Event class:

ServletRequestEvent
Example
@WebListener
public class RequestListener implements ServletRequestListener {

    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println("Request started");
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("Request finished");
    }
}

Useful for:

Logging

Performance timing

Security checks