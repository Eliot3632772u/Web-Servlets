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

username=user&password=123

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







1️⃣ What Is a Servlet Container?

A Servlet Container is a program that:

Runs servlets

Manages their lifecycle

Handles HTTP communication

Creates request/response objects

Manages sessions

Handles threading

Manages application isolation

The most famous example is:

Apache Tomcat

Other examples include:

Jetty

WildFly

What Does the Container Actually Do?

Imagine you write this:

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        res.getWriter().println("Hello");
    }
}

You never:

Open a socket

Parse HTTP

Create threads

Manage sessions

Manage lifecycle

The container does all of this.

Internal Responsibilities of a Servlet Container
1️⃣ HTTP Handling

Opens port (8080)

Listens for TCP connections

Parses HTTP request

Creates request/response objects

2️⃣ Servlet Lifecycle

Loads servlet class

Creates instance

Calls init()

Calls service()

Calls destroy()

3️⃣ Thread Management

For each request:

Creates (or reuses) a thread

Calls servlet.service()

4️⃣ Session Management

Generates JSESSIONID

Stores session objects

Handles timeout

5️⃣ Application Isolation

If you deploy two web apps:

/app1

/app2

They are isolated.

Each has:

Its own classloader

Its own ServletContext

Its own servlets

2️⃣ What Is a “Web Application”?

In a servlet container, an “application” means:

A deployed web module.

Example:

Inside Tomcat:

webapps/
   app1/
   app2/

If you access:

http://localhost:8080/app1
http://localhost:8080/app2

These are two separate web applications.

Each application:

Has its own WEB-INF folder

Has its own classes

Has its own servlets

Has its own ServletContext

3️⃣ What Is ServletContext Exactly?

Now we clarify the confusion.

You misunderstood this part:

❌ “There is one ServletContext per servlet”
✅ Correct statement: There is one ServletContext per web application.

Not per servlet.

Correct Rule

Inside ONE application:

All servlets share the SAME ServletContext object.

Between DIFFERENT applications:

Each application has its OWN ServletContext.

4️⃣ Let’s Visualize It

Suppose Tomcat is running.

You deploy:

/app1

/app2

Internally Tomcat creates:

ServletContext_app1
ServletContext_app2

Now inside app1:

Servlet A

Servlet B

Servlet C

All of them receive:

ServletContext_app1

Inside app2:

Servlet X

Servlet Y

They receive:

ServletContext_app2

So:

✔ Servlets inside the SAME app share context
✔ Different apps have different contexts

5️⃣ Why Do Servlets Share It?

Because during initialization:

Tomcat does something conceptually like:

ServletContext context = new ApplicationContext();

HelloServlet servlet1 = new HelloServlet();
LoginServlet servlet2 = new LoginServlet();

inject(context, servlet1);
inject(context, servlet2);

So both servlets store reference to the SAME object.

That’s why:

context.setAttribute("appName", "MyApp");

in Servlet A

and

context.getAttribute("appName");

in Servlet B

works.

Because they reference the same object in memory.









1️⃣ Where does getServletContext() come from?

getServletContext() is a method defined in:

jakarta.servlet.GenericServlet

And since:

HttpServlet extends GenericServlet

Every servlet automatically inherits:

public ServletContext getServletContext()

So when you write:

ServletContext context = getServletContext();

You are calling a method inherited from GenericServlet.

The Inheritance Chain
Object
   ↓
GenericServlet
   ↓
HttpServlet
   ↓
YourServlet

So your servlet class already has access to:

getServletContext()
2️⃣ How Does It Actually Work Internally?

When the container (like Apache Tomcat) starts your application:

Step 1 — It creates a ServletContext object

One per web application.

Something like:

ServletContext context = new StandardServletContext();

This object represents:

The entire web application

Shared application-level state

Step 2 — It injects it into the servlet during initialization

When Tomcat calls:

servlet.init(ServletConfig config);

ServletConfig contains a reference to ServletContext.

Inside GenericServlet, the code looks conceptually like this:

private ServletConfig config;

public void init(ServletConfig config) {
    this.config = config;
}

public ServletContext getServletContext() {
    return config.getServletContext();
}

So:

getServletContext()
→ config.getServletContext()
→ container’s ServletContext object

It’s just returning the global application context object.

3️⃣ What Is ServletContext Exactly?

ServletContext represents:

The entire web application environment.

There is exactly ONE per deployed web app.

If you deploy two applications:

/app1

/app2

Each has its own ServletContext.

4️⃣ Where Is It Used?

It is used for application-wide shared data and configuration.

Let’s see real examples.

✅ 1. Sharing Data Between Servlets

Servlet A:

ServletContext context = getServletContext();
context.setAttribute("appName", "MyApp");

Servlet B:

ServletContext context = getServletContext();
String name = (String) context.getAttribute("appName");

This works because they share the same context object.

✅ 2. Storing Global Objects (Database Pool)

During startup (via listener):

public void contextInitialized(ServletContextEvent sce) {
    ConnectionPool pool = new ConnectionPool();
    sce.getServletContext().setAttribute("dbPool", pool);
}

Then in any servlet:

ConnectionPool pool = 
    (ConnectionPool) getServletContext().getAttribute("dbPool");

This is very common in real applications.

✅ 3. Accessing Configuration Parameters

In web.xml:

<context-param>
    <param-name>appVersion</param-name>
    <param-value>1.0</param-value>
</context-param>

Then in servlet:

String version = getServletContext().getInitParameter("appVersion");
✅ 4. Getting Real Path of Files
String path = getServletContext().getRealPath("/WEB-INF/config.txt");

Used to read files inside web app.

✅ 5. Logging
getServletContext().log("Application started");
5️⃣ Where Can You Access ServletContext From?

You can get it from:

1️⃣ Inside a Servlet
getServletContext()
2️⃣ From HttpServletRequest
request.getServletContext();

Since Servlet 3.0.

3️⃣ From ServletContextEvent (Listener)
sce.getServletContext();
6️⃣ When Is It Created and Destroyed?

Created:

When the web app starts

When Tomcat deploys the app

Destroyed:

When the app stops

When server shuts down

That’s why it's called application scope.

7️⃣ Important: Thread Safety

ServletContext is shared by:

All users

All sessions

All threads

All servlets

So:

context.setAttribute("counter", 1);

If multiple threads modify the same object → race conditions.

Best practice:
Store thread-safe objects (e.g., ConcurrentHashMap).








1️⃣ What is @WebServlet?

@WebServlet is an annotation that tells the servlet container (like Apache Tomcat):

“This class is a servlet, and it should handle requests for this URL.”

Before annotations, you had to configure this in web.xml.

Now you just put:

@WebServlet("/report")

And that’s enough.

2️⃣ The Most Important Rule

A class annotated with @WebServlet:

✔ Must extend HttpServlet
✔ Must define at least one URL pattern

3️⃣ The Simplest Example
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/report")
public class MoodServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.getWriter().println("This is the report page.");
    }
}

Now if you visit:

http://localhost:8080/yourApp/report

Tomcat will call this servlet.

That’s it.

4️⃣ value vs urlPatterns

These two are basically the same.

✅ When using only URL

You can write:

@WebServlet("/report")

This uses the value attribute automatically.

It is the same as:

@WebServlet(value = "/report")
✅ When using multiple settings

If you also want to define other properties (like init parameters), then use urlPatterns.

Example:

@WebServlet(
    urlPatterns = "/report",
    loadOnStartup = 1
)

Use urlPatterns when there are multiple attributes.

5️⃣ What Happens Internally?

When Tomcat starts:

It scans your classes

Finds @WebServlet

Creates a mapping:

"/report" → MoodServlet

Creates one instance of MoodServlet

Calls init()

Waits for requests

6️⃣ What Is init()?

init() runs once, when the servlet is created.

Used for:

Reading configuration

Opening files

Connecting to database

Loading resources

Example: Overriding init()
@WebServlet("/report")
public class MoodServlet extends HttpServlet {

    private String message;

    @Override
    public void init() {
        message = "Report servlet initialized!";
        System.out.println(message);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.getWriter().println(message);
    }
}

When server starts → prints initialization message.

7️⃣ What Are initParams?

Sometimes a servlet needs configuration.

Instead of hardcoding values, you can pass parameters.

Example Using @WebInitParam
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.WebInitParam;

@WebServlet(
    urlPatterns = "/report",
    initParams = {
        @WebInitParam(name = "author", value = "user")
    }
)
public class MoodServlet extends HttpServlet {

    private String author;

    @Override
    public void init() {
        author = getServletConfig().getInitParameter("author");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.getWriter().println("Author: " + author);
    }
}

When you visit /report, it prints:

Author: user
8️⃣ What Is UnavailableException?

If something goes wrong during initialization:

@Override
public void init() throws ServletException {
    if (someProblem) {
        throw new UnavailableException("Database not available");
    }
}

Tomcat will:

Not allow servlet to handle requests

Log error

This is used when startup fails.

9️⃣ What Is the Difference Between Init Parameter and Context Parameter?

Very important.

🔹 Servlet Init Parameter

✔ Belongs to ONE specific servlet
✔ Accessed using:

getServletConfig().getInitParameter("name");

Used for:

Servlet-specific configuration

🔹 Context Parameter

✔ Belongs to the entire application
✔ Shared by all servlets
✔ Accessed using:

getServletContext().getInitParameter("name");

Defined in web.xml (usually).

Used for:

Global configuration (DB URL, version, etc.)












🔎 What Is a Servlet Filter?

A Servlet Filter is a special Java class that sits between the client (browser) and your servlet.

It can:

Inspect a request before it reaches a servlet

Modify the request

Block the request

Let it continue

Modify the response before it goes back to the client

It does not usually generate responses itself like a servlet does.

Think of it as a gatekeeper or interceptor.

🧠 What Problem Do Filters Solve?

Imagine you have 20 servlets in your app.

You want to:

Log every request

Check if user is logged in

Compress responses

Add security headers

Measure execution time

Without filters ❌
You would need to duplicate that logic in every servlet.

With filters ✅
You write the logic once, and it applies to many servlets.

🏗 Where Filters Fit in the Architecture

In a servlet container like:

Apache Tomcat

Eclipse Jetty

The request flow looks like this:

Browser
   ↓
Filter 1
   ↓
Filter 2
   ↓
Servlet
   ↓
Filter 2 (response phase)
   ↓
Filter 1 (response phase)
   ↓
Browser

Filters wrap around servlets.

🔹 What Is a Filter Technically?

A filter is a class that implements:

javax.servlet.Filter

It must implement:

init()
doFilter()
destroy()

The most important method is:

doFilter()
🔥 Basic Example: Logging Filter
Step 1 — Create a Filter
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")   // Apply to all URLs
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        System.out.println("LoggingFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        System.out.println("Request received");

        // Pass request to next filter or servlet
        chain.doFilter(request, response);

        System.out.println("Response sent");
    }

    @Override
    public void destroy() {
        System.out.println("LoggingFilter destroyed");
    }
}
🔎 What Is chain.doFilter()?

This is VERY important.

If you call:

chain.doFilter(request, response);

The request continues to:

The next filter

Or the target servlet

If you DO NOT call it:

The request is blocked.

🚫 Example: Authentication Filter (Blocking Requests)
@WebFilter("/admin/*")
public class AuthFilter implements Filter {

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect("/login");
            return;   // STOP request
        }

        chain.doFilter(request, response); // Continue if logged in
    }
}
What this solves:

Instead of checking login in every admin servlet,
we do it in one place.

🔄 Filter Lifecycle

In containers like Apache Tomcat:

Filter class is loaded

init() runs once

doFilter() runs per request

destroy() runs when server shuts down

🎯 URL Pattern Mapping

You control where the filter applies using:

@WebFilter("/*")        // All requests
@WebFilter("/admin/*")  // Only admin URLs
@WebFilter("/report")   // Specific servlet
🔗 What Is a Filter Chain?

Multiple filters can apply to the same servlet.

Example:

@WebFilter("/*")
public class LoggingFilter { ... }

@WebFilter("/*")
public class SecurityHeaderFilter { ... }

@WebFilter("/admin/*")
public class AuthFilter { ... }

If user requests:

/admin/dashboard

The chain might be:

LoggingFilter
    ↓
SecurityHeaderFilter
    ↓
AuthFilter
    ↓
AdminServlet

Order depends on mapping order in web.xml
(or container behavior with annotations).

🧩 Modifying Request or Response

Filters can wrap requests or responses.

Example: Add Header to Every Response
@WebFilter("/*")
public class SecurityHeaderFilter implements Filter {

    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse res = (HttpServletResponse) response;

        chain.doFilter(request, response);

        res.setHeader("X-Content-Type-Options", "nosniff");
    }
}

Now every response gets a security header.

📦 What Is a Request/Response Wrapper?

If you want to:

Modify request parameters

Change response body

Capture output

You wrap them:

HttpServletRequestWrapper

HttpServletResponseWrapper

Example:

public class CustomRequest extends HttpServletRequestWrapper {
    public CustomRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        if (name.equals("username")) {
            return super.getParameter(name).toUpperCase();
        }
        return super.getParameter(name);
    }
}

Then pass it:

chain.doFilter(new CustomRequest(req), response);


🔁 What Is a Dispatcher Type in Servlet Filters?

A Dispatcher Type controls when a filter should run during request processing.

Not every request reaches a servlet the same way.

Sometimes:

The browser calls a servlet directly

One servlet forwards to another

A JSP includes another resource

An error page is triggered

An async process resumes

Dispatcher types let you say:

“Run this filter only in specific situations.”

🧠 Why Dispatcher Types Exist

Imagine you have an authentication filter.

You probably want it to run when:

A user directly accesses /admin

But maybe NOT when:

The request is internally forwarded

An error page is being displayed

Without dispatcher types, your filter would run in all cases — sometimes unnecessarily.

📌 The 5 Dispatcher Types

These are defined in javax.servlet.DispatcherType.

1️⃣ REQUEST (Most Common)
Meaning:

The request came directly from the client (browser).

Example:

User types:
http://localhost:8080/app/login

The filter with REQUEST will run.

Typical Use:

✔ Authentication
✔ Logging
✔ Security checks

2️⃣ FORWARD
Meaning:

A servlet forwarded the request to another resource using:

request.getRequestDispatcher("/dashboard").forward(request, response);

Now /dashboard is being processed.

If a filter has FORWARD, it runs during that internal forward.

Example Flow:
Browser → LoginServlet
LoginServlet → forward() → DashboardServlet

If filter supports:

REQUEST → runs for LoginServlet

FORWARD → runs for DashboardServlet

3️⃣ INCLUDE
Meaning:

A resource was included inside another resource using:

request.getRequestDispatcher("/header.jsp").include(request, response);

Common in JSP pages.

Example:

main.jsp includes header.jsp

If filter supports INCLUDE → it runs for header.jsp.

4️⃣ ERROR
Meaning:

The request is being processed by the error handling mechanism.

Example:

<error-page>
    <error-code>404</error-code>
    <location>/error.jsp</location>
</error-page>

If a 404 happens:

Original request → error.jsp

If filter supports ERROR → it runs for error.jsp.

5️⃣ ASYNC
Meaning:

The request is resumed in asynchronous processing.

Example:

AsyncContext async = request.startAsync();

Later, when async resumes, filters with ASYNC dispatcher will run.

Used in advanced non-blocking servlet applications.

🎯 How To Specify Dispatcher Type
Using Annotation
@WebFilter(
    urlPatterns = "/*",
    dispatcherTypes = {
        DispatcherType.REQUEST,
        DispatcherType.FORWARD
    }
)

This filter runs:

On direct client request

On forward

But NOT on include or error.

>
🔎 Visual Example

Suppose we have:

@WebFilter(urlPatterns = "/*",
           dispatcherTypes = {DispatcherType.REQUEST})
Scenario:
Browser → ServletA → forward → ServletB

Filter runs:

✔ For ServletA (REQUEST)

❌ Not for ServletB (because it is FORWARD)

If we change to:

dispatcherTypes = {
    DispatcherType.REQUEST,
    DispatcherType.FORWARD
}

Now filter runs:

✔ ServletA

✔ ServletB

🧠 Real-World Example: Authentication

Imagine:

/login

/dashboard

/error.jsp

You want:

Authentication for direct access

But NOT re-authentication during forward

And NOT on error pages

You use:

dispatcherTypes = {DispatcherType.REQUEST}
⚠️ What Happens If You Don’t Specify?

Default behavior:

DispatcherType.REQUEST only

So if you don't specify anything, the filter runs only for direct client requests.









🔁 Invoking Other Web Resources (Servlet → Servlet / JSP / HTML)

In a Java web application running inside a servlet container like:

Apache Tomcat

Eclipse Jetty

A web component (Servlet/JSP) can call another web resource in two ways:

1️⃣ Indirectly

Send a URL to the browser and let the browser make a new request.

Example:

<a href="/app/profile">Go to Profile</a>

Browser makes a new HTTP request.

2️⃣ Directly (Server-Side Invocation)

This happens inside the server, without the browser knowing.

Two ways:

include() → include content inside current response

forward() → transfer control completely

These use a RequestDispatcher.

🧠 Why Do We Need This?

Because real applications are modular.

Example problems:

You want a header and footer on every page.

You want a controller servlet to decide which view (JSP) to show.

You want authentication logic in one servlet and rendering in another.

You want to reuse components.

Without forwarding/include:
You would duplicate code everywhere.

🎯 What Is RequestDispatcher?

RequestDispatcher is an object that allows one web component to:

Include another resource

Forward a request to another resource

You obtain it using:

request.getRequestDispatcher("path");

or

getServletContext().getRequestDispatcher("path");
⚙️ How to Get a RequestDispatcher
From request (can use relative path)
RequestDispatcher rd =
    request.getRequestDispatcher("dashboard.jsp");

Relative path allowed.

From ServletContext (must use absolute path)
RequestDispatcher rd =
    getServletContext().getRequestDispatcher("/dashboard.jsp");

Must start with /.

If resource does not exist:

rd == null

You must handle that safely.

🔹 PART 1: Including Another Resource
What Does include() Do?

It:

Calls another resource

Inserts its output into the current response

Returns control back to the caller

The original servlet continues executing.

🧩 Example: Header + Footer Reuse
HeaderServlet
@WebServlet("/header")
public class HeaderServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.getWriter().println("<h1>My Website</h1>");
    }
}
MainServlet
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException, ServletException {

        response.getWriter().println("<html><body>");

        RequestDispatcher rd =
            request.getRequestDispatcher("/header");

        rd.include(request, response);

        response.getWriter().println("<p>Welcome!</p>");
        response.getWriter().println("</body></html>");
    }
}
🧠 What Happens Internally?
Browser → /home

HomeServlet:
    prints <html>
    include(header)
        HeaderServlet runs
        header output inserted
    continues printing

Final output:

<html>
<h1>My Website</h1>
<p>Welcome!</p>
</html>

🔹 PART 2: Forwarding to Another Resource
What Does forward() Do?

It:

Stops current servlet execution

Transfers control completely

The forwarded resource generates the response

Browser URL does NOT change

🧠 Common Use Case: MVC Pattern

Controller → View

LoginServlet (Controller)
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException, ServletException {

        String user = request.getParameter("username");

        if (user.equals("admin")) {
            request.setAttribute("message", "Welcome Admin!");

            RequestDispatcher rd =
                request.getRequestDispatcher("/dashboard.jsp");

            rd.forward(request, response);
        } else {
            response.sendRedirect("error.html");
        }
    }
}
dashboard.jsp
<h2>${message}</h2>
🧠 What Happens Internally?
Browser → POST /login
LoginServlet processes
forward → dashboard.jsp
dashboard.jsp generates response
Browser sees result

BUT URL still shows:

/login

Because forward is server-side.

🔥 Important Rule About forward()

You cannot write to response before forwarding.

This will throw:

IllegalStateException

Bad:

response.getWriter().println("Hello");
rd.forward(request, response);  // ❌ ERROR

Why?

Because response was already committed.








🧠 Maintaining Client State (Sessions in Servlets)
🚨 The Core Problem: HTTP Is Stateless

HTTP does not remember anything between requests.

Example:

Request 1 → Add item to cart
Request 2 → Checkout

The server does not automatically know these two requests came from the same user.

That’s a problem for:

Shopping carts

Login systems

Multi-step forms

User preferences

Dashboards

So we need Session Management.

🎯 What Is a Session?

A Session is a server-side object that stores data for one specific user across multiple requests.

In Java Servlets, sessions are represented by:

javax.servlet.http.HttpSession
🔹 How It Works Internally

In a container like:

Apache Tomcat

When a user visits your site:

Server creates a unique session ID.

Server stores session data in memory.

Server sends session ID to browser (usually as cookie: JSESSIONID).

Browser sends that ID back with every request.

Server finds correct session using that ID.

🔑 Accessing a Session

You get session from request:

HttpSession session = request.getSession();
What this does:

If session exists → returns it.

If not → creates new session.

If you DON’T want to create one:
HttpSession session = request.getSession(false);

Returns null if no session exists.

🛒 Example: Shopping Cart
Step 1 — Add Item
@WebServlet("/add")
public class AddToCartServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();

        List<String> cart =
            (List<String>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        String item = request.getParameter("item");
        cart.add(item);

        session.setAttribute("cart", cart);

        response.getWriter().println("Item added!");
    }
}
Step 2 — View Cart
@WebServlet("/cart")
public class ViewCartServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.getWriter().println("No cart found");
            return;
        }

        List<String> cart =
            (List<String>) session.getAttribute("cart");

        response.getWriter().println("Your Cart:");
        for (String item : cart) {
            response.getWriter().println(item);
        }
    }
}
🧩 Associating Objects With Session

You store objects like this:

session.setAttribute("username", "john");

Retrieve:

String user = (String) session.getAttribute("username");

Remove:

session.removeAttribute("username");
🧠 Important Concept

Session data:

✔ Available to all servlets
✔ Within the same web application
✔ For the same user session

🔥 Session Timeout

Because browser never tells server:

“I’m done.”

Sessions expire automatically after inactivity.

Default: usually 30 minutes.

Get Timeout
int seconds = session.getMaxInactiveInterval();
Set Timeout (in seconds)
session.setMaxInactiveInterval(600); // 10 minutes

❌ Invalidating Session (Logout)
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.getWriter().println("Logged out");
    }
}

What this does:

✔ Deletes session
✔ Removes all session data
✔ Makes session ID invalid

🔔 Session Events (Advanced)

Your objects can listen to session events.

1️⃣ HttpSessionBindingListener

Notified when object is:

Added to session

Removed from session

Example:

public class User implements HttpSessionBindingListener {

    public void valueBound(HttpSessionBindingEvent event) {
        System.out.println("User added to session");
    }

    public void valueUnbound(HttpSessionBindingEvent event) {
        System.out.println("User removed from session");
    }
}




🧠 What Is “Finalizing a Servlet”?

Finalizing means:

The servlet container is about to remove the servlet from service.

This happens when:

The server shuts down

The application is redeployed

The container wants to free memory

The servlet is unloaded

Before removal, the container calls:

public void destroy()
🎯 Why destroy() Exists

Your servlet may hold resources like:

Database connections

File handles

Network sockets

Threads

Caches

If you don’t clean them up:

Memory leaks occur

Connections remain open

Threads continue running

Server may crash

So destroy() is your cleanup method.

⚠️ Important Detail

The container tries to call destroy() only after:

All active requests finish
OR

A grace period expires

But if a request takes too long…

👉 It might still be running when destroy() executes.

That’s the real problem this section solves.

🚨 The Real Problem

Imagine this situation:

protected void doPost(...) {
    Thread.sleep(5 minutes);
}

Now:

User sends request

Servlet starts long task

Admin shuts down server

Container calls destroy()

But that thread is still running!

If you immediately close resources:

Thread may crash

Data may corrupt

Exceptions may happen

So we need:

✔ Track running requests
✔ Notify them to stop
✔ Wait for them to finish
✔ Then clean up safely

🧮 Step 1 — Tracking Active Service Methods

Because servlets are multi-threaded:

Each request runs in a different thread.

So we count how many are active.

Add Counter Field
public class ShutdownExample extends HttpServlet {

    private int serviceCounter = 0;

    protected synchronized void enteringServiceMethod() {
        serviceCounter++;
    }

    protected synchronized void leavingServiceMethod() {
        serviceCounter--;
    }

    protected synchronized int numServices() {
        return serviceCounter;
    }
}
Why synchronized?

Multiple threads will modify serviceCounter.

Without synchronization:

Race conditions occur

Counter becomes incorrect

🔁 Step 2 — Override service() Properly

Normally you override:

doGet()
doPost()

But here we override service() itself.

Why?

Because service() is called for EVERY request.

Correct Implementation
protected void service(HttpServletRequest req,
                       HttpServletResponse resp)
        throws ServletException, IOException {

    enteringServiceMethod();

    try {
        super.service(req, resp);
    } finally {
        leavingServiceMethod();
    }
}
Why use try-finally?

Even if:

Exception occurs

Client disconnects

We must decrement counter.

🔔 Step 3 — Shutdown Notification Flag

We need a way to tell running threads:

“Stop working. Server is shutting down.”

Add a flag:

private boolean shuttingDown;

protected synchronized void setShuttingDown(boolean flag) {
    shuttingDown = flag;
}

protected synchronized boolean isShuttingDown() {
    return shuttingDown;
}
🛑 Step 4 — Implement destroy() Cleanly
public void destroy() {

    // If requests still running
    if (numServices() > 0) {
        setShuttingDown(true);
    }

    // Wait for them to finish
    while (numServices() > 0) {
        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
        }
    }

    // Now safe to release resources
    closeDatabaseConnection();
}
🧠 What Happens Now?

When shutdown starts:

destroy() is called

shuttingDown flag becomes true

destroy() waits

Running threads detect shutdown

They stop politely

Counter reaches zero

destroy() exits safely