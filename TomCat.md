What Is Apache Tomcat?

Apache Tomcat is an open-source Java web server and servlet container developed by the Apache Software Foundation.

It is designed to:

Run Java Servlets

Run JSP (JavaServer Pages)

Implement core parts of the Jakarta EE (formerly Java EE) web specifications

In simple terms:

Tomcat is the software that receives HTTP requests (like from a browser), runs your Java servlet code, and sends back an HTTP response.

1️⃣ What Problem Does Tomcat Solve?

Java servlets are just Java classes. By themselves, they cannot:

Listen for HTTP requests

Open ports

Parse HTTP headers

Manage sessions

Handle concurrency

Manage servlet lifecycle

Tomcat solves this by acting as:

Role	What It Does
Web Server	Listens on port 8080 (or 80)
Servlet Container	Manages servlet lifecycle
JSP Engine	Converts JSP into servlets
Thread Manager	Handles multiple users at once
Session Manager	Tracks user sessions
Request Dispatcher	Routes requests to correct servlet

Without Tomcat (or another container), servlets cannot run.

3️⃣ How Tomcat Works Internally

Tomcat consists of several main components:

🔹 Connector

Listens on a port (default 8080)

Accepts HTTP requests

Converts raw socket data into request objects

🔹 Engine

Processes requests

Routes to correct web application

🔹 Host

Represents a virtual host (like localhost)

🔹 Context

Represents a web application

🔹 Servlet Container (Catalina)

Manages servlet lifecycle:

init()

service()

destroy()




2️⃣ What is a WAR Package?

WAR = Web Application Archive

It is a .war file that contains:

Compiled .class files

web.xml

Libraries (JARs)

Static resources (HTML, CSS, JS)

Think of WAR as:

.zip file for web applications

Example:

myapp.war

Tomcat deploys .war files automatically when placed inside:

tomcat/webapps/
3️⃣ Why This File Structure Exists

You showed:

myapp/
│
└── WEB-INF/
    ├── web.xml
    └── classes/
         └── HelloServlet.class

Let’s understand this.

🔹 What is WEB-INF?

WEB-INF is a protected folder.

✔ Files inside cannot be accessed directly via browser
✔ Only the server (Tomcat) can access them

If someone tries:

http://localhost:8080/myapp/WEB-INF/web.xml

They get 404.

Security reason:

You don’t want users downloading .class files

You don’t want users reading configuration

🔹 Why classes/ exists?

Tomcat must know where compiled classes are located.

Standard structure inside a WAR:

WEB-INF/
   classes/        ← compiled .class files
   lib/            ← jar dependencies
   web.xml         ← configuration

Tomcat loads classes from:

WEB-INF/classes
WEB-INF/lib

That’s why the structure exists — it follows the Java Servlet specification.

4️⃣ Now Let’s Do It Properly With Maven

Modern Maven project structure is different from the old manual one.

🏗 Step 1: Create a Maven Web Project

Create project:

mvn archetype:generate

Or manually create structure:

myapp/
├── pom.xml
└── src/
    └── main/
        ├── java/
        ├── resources/
        └── webapp/
            └── WEB-INF/

This is the standard Maven web structure.

🧾 Step 2: pom.xml

Set packaging to WAR:

<project>
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>myapp</artifactId>
    <version>1.0-SNAPSHOT</version>

    <packaging>war</packaging>

    <dependencies>
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>5.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>

⚠ Important:
scope=provided means:

Tomcat provides Servlet API

It won’t be packaged inside WAR

🧑‍💻 Step 3: Create a Servlet

File:

src/main/java/com/example/HelloServlet.java
package com.example;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.getWriter().println("<h1>Hello from Servlet!</h1>");
    }
}
📝 Step 4: web.xml

Location:

src/main/webapp/WEB-INF/web.xml
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         version="5.0">

    <servlet>
        <servlet-name>HelloServlet</servlet-name>
        <servlet-class>com.example.HelloServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>HelloServlet</servlet-name>
        <url-pattern>/hello</url-pattern>
    </servlet-mapping>

</web-app>

This tells Tomcat:

Which class is a servlet

Which URL triggers it

🏗 Step 5: Build the WAR

Run:

mvn clean package

Maven will:

Compile Java → .class

Copy them into:

target/myapp/WEB-INF/classes/

Create:

target/myapp.war
📦 What WAR Actually Contains

If you unzip the WAR:

myapp.war
│
├── META-INF/
├── WEB-INF/
│   ├── web.xml
│   ├── classes/
│   │   └── com/example/HelloServlet.class
│   └── lib/
└── (static files if any)

This matches the structure you asked about earlier.

🚀 Step 6: Deploy to Tomcat

Copy WAR to:

apache-tomcat/webapps/

Start Tomcat:

bin/startup.sh

Tomcat automatically:

Detects myapp.war

Extracts it into:

webapps/myapp/

Reads web.xml

Loads servlets

Starts serving requests

🌍 Access Your Servlet

Open browser:

http://localhost:8080/myapp/hello

You’ll see:

Hello from Servlet!