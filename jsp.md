1. What Is a JSP Page (Conceptually)?

JavaServer Pages (JSP) is a server-side view technology used to generate dynamic web content using Java.

A JSP page is not a script that runs in the browser.
Instead, it runs on the server, and its output is usually HTML sent to the client.

Core idea

JSP = HTML + Java-based dynamic logic (executed on the server)

A JSP page contains:

Static content

HTML, XML, SVG, WML, plain text

Sent to the browser as-is

JSP elements

Special tags and expressions that are evaluated on the server

Used to generate dynamic content

2. Why JSP Exists (The Problem It Solves)
The problem before JSP

Before JSP, developers used Java Servlet directly:

out.println("<html>");
out.println("<body>");
out.println("<h1>Hello</h1>");
out.println("</body>");
out.println("</html>");

This caused:

❌ Extremely messy code

❌ Hard to read and maintain

❌ Poor separation between logic and presentation

What JSP solves

JSP flips the model:

<h1>Hello ${user.name}</h1>

Now:

HTML stays HTML

Java logic stays Java

Designers and backend developers can work independently

👉 JSP solves the “Servlet HTML hell” problem

3. JSP Files and Structure
.jsp

Full JSP page

Can respond directly to HTTP requests

.jspf

JSP fragment

Cannot be accessed directly

Used with <%@ include %>

<%@ include file="header.jspf" %>

This enables:

Layout reuse

Cleaner architecture

Template-style composition

4. JSP Syntax Types

JSP supports two syntaxes:

1. Standard syntax (most common)
<%@ page contentType="text/html" %>
${user.name}
2. XML syntax
<jsp:root>

XML syntax exists mainly for:

XML tooling

Validation

Programmatic manipulation

⚠️ You cannot mix both in one file

5. JSP Elements Explained (Very Important)
5.1 Page Directive
<%@ page contentType="text/html;charset=UTF-8" %>

Controls how the JSP is translated and executed:

Content type

Buffer size

Error handling

Threading behavior

It affects the generated servlet, not runtime logic directly.

5.2 Taglib Directive
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

Imports tag libraries, usually JSTL.

Why this matters:

Eliminates Java code in JSP

Makes JSP declarative

Safer and cleaner

5.3 JavaBeans (jsp:useBean)
<jsp:useBean id="locales" class="com.example.LocaleBean" />

What it does:

Creates (or reuses) a Java object

Stores it in a scope (page, request, session, application)

Equivalent servlet logic:

LocaleBean locales = new LocaleBean();
5.4 Expression Language (EL)
${user.email}

What happens:

Calls getEmail() internally

No Java code needed

Null-safe

Readable

This replaces:

<%= user.getEmail() %>
5.5 Custom Tags (JSTL)

Examples:

<c:forEach items="${locales}" var="loc">
<c:if test="${loc == selected}">

These:

Compile into Java method calls

Are implemented using Tag Handlers

Avoid scriptlets completely

6. The JSP Life Cycle (Critical Section)
Key idea:

A JSP page is just a servlet in disguise

Step 1: Request comes in

User requests:

/index.jsp
Step 2: Translation

The container (e.g. Apache Tomcat) checks:

Is the .jsp newer than the compiled servlet?

If yes:
➡ JSP is translated into a Java servlet

Example:

index.jsp → index_jsp.java
Step 3: Compilation

The generated servlet source is compiled:

index_jsp.java → index_jsp.class

Errors here:

Syntax errors

Invalid tag usage

EL issues

Returned as:

JasperException
Step 4: Servlet Life Cycle

The JSP servlet follows the normal servlet lifecycle:

Load class

Instantiate

jspInit()

_jspService(request, response) ← runs on every request

jspDestroy()

⚠️ You never write _jspService() yourself — JSP generates it.

7. What Happens During Translation (Internals)
JSP Content	Converted To
Static HTML	out.write("...")
Directives	Translation instructions
EL (${})	Runtime expression evaluation
jsp:setProperty	Java method calls
jsp:include	RequestDispatcher.include()
Custom tags	Tag handler method calls

This is why JSP performance ≈ Servlet performance.

8. Output Buffering
<%@ page buffer="8kb" %>

Why buffering exists:

Allows headers & status codes to be set before response is committed

Enables forwarding and error handling

Tradeoff:

Large buffer → more memory

Small buffer → faster client response

9. JSP Error Handling (Very Important)
Declaring an error page
<%@ page errorPage="error.jsp" %>

If an exception occurs:
➡ Request is forwarded to error.jsp

Error page declaration
<%@ page isErrorPage="true" %>

This:

Enables access to ErrorData

Exposes exception details via pageContext

Accessing error info
${pageContext.errorData.statusCode}
${pageContext.errorData.throwable}
${pageContext.errorData.throwable.cause}

This allows:

Friendly error pages

Debug information

Centralized error handling




1️⃣ What “Creating Dynamic Content” Really Means

Dynamic content means:

The HTML returned to the client depends on data from Java objects.

Example:

Instead of:

<h1>Welcome John</h1>

You write:

<h1>Welcome ${user.firstName}</h1>

Now the page is dynamic — it changes per user.

Where does user come from?

That’s what this section explains.

2️⃣ How JSP Accesses Objects

JSP can access three types of objects:

Implicit objects (automatically provided)

Application-specific objects (your own JavaBeans)

Shared objects (objects stored in different scopes)

Let’s analyze each deeply.

3️⃣ Implicit Objects (Automatically Created)

Implicit objects are provided by the web container automatically.

You don’t create them.
You don’t import them.
They just exist.

They come from Servlet API.

Here are the main ones:

Object	Type	Purpose
request	HttpServletRequest	Data from client
response	HttpServletResponse	Response to client
session	HttpSession	User session data
application	ServletContext	Global app data
out	JspWriter	Output stream
pageContext	PageContext	Central access object
config	ServletConfig	Servlet configuration
page	Object (this)	JSP servlet instance
Example: Using request
${param.username}

This retrieves:

request.getParameter("username")
Example: Using session
${sessionScope.user}

Equivalent to:

session.getAttribute("user")
Why Implicit Objects Exist

They solve this problem:

Without them, you would need:

<%
String name = request.getParameter("username");
%>

With EL:

${param.username}

Much cleaner.
No Java code inside HTML.

4️⃣ Application-Specific Objects (JavaBeans)

This is where real application logic belongs.

The book emphasizes:

Page designers should focus on presentation.
Java developers should focus on logic.

So business logic is encapsulated inside Java classes.

Example JavaBean:

public class User {
    private String firstName;
    private String email;

    public String getFirstName() { return firstName; }
    public String getEmail() { return email; }
}

In JSP:

<jsp:useBean id="user" class="com.example.User" scope="request" />
${user.firstName}

EL automatically calls:

user.getFirstName()
Why JavaBeans?

Because they follow strict conventions:

Private fields

Public getters/setters

No business logic in JSP

This solves:

Maintainability problems

Mixing Java with HTML

Hard-to-debug code

5️⃣ Scopes in JSP (Very Important)

Objects can live in 4 scopes:

Scope	Lifetime	Visible To
page	One request, one page	Only this JSP
request	One HTTP request	Forwarded pages
session	One user session	All pages for user
application	Entire app lifetime	All users

Example:

<jsp:useBean id="cart" class="Cart" scope="session" />

That cart is shared across pages for that user.

6️⃣ Shared Objects and Concurrency (CRITICAL)

This is where things become advanced.

Remember:

A JSP is a servlet.
Servlets are multithreaded.

That means:

Multiple users can hit the same JSP at the same time.

What Does That Mean?

Let’s say:

<%! int counter = 0; %>
<%
counter++;
%>

That counter is a field of the servlet class.

If 100 users request the page at the same time:

All threads modify the same counter.

Race conditions happen.

7️⃣ The isThreadSafe Directive
<%@ page isThreadSafe="true" %>

Default: true

Means:

The container can handle multiple requests concurrently.

You must manage synchronization yourself.

What Happens Internally?

If true:

The servlet behaves like:

public class MyJspServlet extends HttpServlet {
    public void _jspService(...) {
        // executed by many threads
    }
}

Multiple threads enter _jspService() at once.

8️⃣ If isThreadSafe="false"

Then:

The container ensures:

Only one request at a time enters _jspService()

Internally, the servlet implements:

javax.servlet.SingleThreadModel

BUT…

🚨 SingleThreadModel is deprecated.

Why?

Because:

It kills scalability

It may create multiple servlet instances anyway

It wastes memory

That’s why the book says:

Not recommended.









1️⃣ Why Expression Language Exists

Before EL, JSP used scriptlets:

<%
User user = (User) session.getAttribute("user");
out.println(user.getName());
%>

Problems:

Java mixed with HTML

Hard to maintain

Hard to read

Security risks

So JSP 2.0 introduced:

${user.name}

Much cleaner.

2️⃣ JSP 2.0 Expression Language (Original EL)

JSP 2.0 EL was:

Simple

Read-only

Immediate evaluation

Designed for request/response rendering

Example:

<c:if test="${sessionScope.cart.numberOfItems > 0}">
    You have items!
</c:if>

This:

Looks in session scope

Finds cart

Calls getNumberOfItems()

Compares to 0

Equivalent Java:

Cart cart = (Cart) session.getAttribute("cart");
if (cart.getNumberOfItems() > 0) {
    ...
}
3️⃣ Why JSP EL Was Limited

JSP has a simple lifecycle:

Request

Render HTML

Done

It only needs to read data and render output.

So JSP 2.0 EL:

Could NOT invoke methods

Could NOT set values

Could NOT defer execution

4️⃣ JavaServer Faces (JSF) Needed More Power

JavaServer Faces has a complex lifecycle:

Restore view

Apply request values

Process validations

Update model values

Invoke application logic

Render response

Because of this, JSF needed:

Deferred evaluation

Method invocation

Two-way binding (get + set)

So JSF created a more powerful EL.

5️⃣ Why They Unified Them

Before JSP 2.1:

JSP had one EL

JSF had another EL

Problem:

Incompatibility

Tag conflicts

Mixed pages were difficult

So JSP 2.1 introduced:

Unified EL

A single expression language used by:

JSP

JSF

Custom tag libraries

6️⃣ What Unified EL Can Do

It can:

✅ Read properties
✅ Write properties
✅ Invoke methods
✅ Do arithmetic
✅ Perform logical operations
✅ Access maps, lists, arrays
✅ Use custom resolvers


1️⃣ The Core Idea

The difference is NOT just syntax.

It is about WHEN the expression is evaluated.

Syntax	Type	When evaluated	Who evaluates it
${}	Immediate	Right away during page rendering	JSP engine
#{}	Deferred	Later, at specific lifecycle phases	JSF or other framework
2️⃣ Immediate Evaluation (${})
What It Means

When the JSP page is rendered:

JSP engine encounters ${...}

It evaluates the expression immediately

Replaces it with the value

Sends HTML to the browser

There is no future re-evaluation.

Example (JSP + JSTL)
<fmt:formatNumber value="${sessionScope.cart.total}"/>
What happens internally:

JSP translates page into servlet.

During _jspService() execution:

EL is evaluated.

sessionScope.cart.total is resolved.

Result is passed to fmt:formatNumber.

Equivalent Java:

Cart cart = (Cart) session.getAttribute("cart");
double total = cart.getTotal();
formatNumberTag.setValue(total);

After rendering:

Expression is gone.

Only final HTML remains.

Important: Immediate Expressions Are Read-Only

You can do:

${customer.name}
${cart.total}
${user.age + 10}

But you CANNOT:

${customer.setName("John")}  ❌

Why?

Because JSP lifecycle is simple:

Request → Render → Done

There is no later phase to update model.

3️⃣ Deferred Evaluation (#{})

Deferred evaluation was introduced by:

JavaServer Faces

Why?

Because JSF has a multiphase lifecycle.

4️⃣ JSF Lifecycle (Critical to Understand)

JSF lifecycle phases:

Restore View

Apply Request Values

Process Validations

Update Model Values

Invoke Application

Render Response

Because of this, JSF needs to:

Read values

Validate them

Update JavaBeans

Possibly re-render page

Possibly call methods

So it cannot evaluate expressions immediately.

It must defer evaluation.

5️⃣ Example of Deferred Value Expression
<h:inputText id="name" value="#{customer.name}" />

Let’s walk through two scenarios.

Scenario 1: First Page Load (Initial Request)

User visits page.

Lifecycle phase: Render Response

JSF evaluates:

customer.getName()

It uses value to pre-fill the input field.

This behaves like immediate evaluation.

Scenario 2: Form Submission (Postback)

User submits new name.

Now lifecycle:

Phase 2 – Apply Request Values

JSF retrieves input from HTTP request.

Phase 3 – Validate

Checks constraints.

Phase 4 – Update Model Values

Now JSF calls:

customer.setName(submittedValue);

This is possible because #{customer.name} is an Lvalue expression.

6️⃣ Why ${} Cannot Do This

${} expressions are evaluated immediately during rendering.

After rendering:

Expression disappears.

JSP forgets it existed.

There is no link back to the JavaBean.

#{} keeps a reference to the property and framework can call getter and setter later.

7️⃣ Immediate vs Deferred — Step-by-Step Comparison
JSP Immediate
<p>${user.name}</p>

Steps:

JSP sees ${user.name}

Calls getName()

Outputs <p>John</p>

Done forever

No setter possible.

JSF Deferred
<h:inputText value="#{user.name}" />

Steps on initial render:

Call getName()

Render value

Steps on postback:

Retrieve form value

Validate

Call setName(value)

Possibly call business logic

Re-render

8️⃣ Deferred Method Expressions

Unified EL allows method invocation.

Example:

<h:commandButton value="Save" action="#{user.save}" />

When button is clicked:

JSF calls:

public String save() {
    // business logic
    return "success";
}

9️⃣ Internal Representation

When using ${}:

The container stores only a ValueExpression (read-only).

When using #{}:

The container stores a ValueExpression or MethodExpression object.

Later JSF can call:

valueExpression.getValue(context);
valueExpression.setValue(context, newValue);

or

methodExpression.invoke(context, params);

That’s the key difference.











🔷 PART 1 — The Two Fundamental Expression Types

Unified EL defines two categories:

Value Expressions

Method Expressions

Think of it like this:

Value expression → refers to data

Method expression → refers to behavior

🔶 PART 2 — Value Expressions

A value expression refers to a value inside an object.

It can either:

🔹 Read data (rvalue)

🔹 Read + Write data (lvalue)

🔹 2.1 Rvalue Expressions (Read-only)

These can only read values.

All ${} expressions are always rvalue.

Example:

<taglib:tag value="${customer.name}" />

What happens internally:

customer.getName();

You cannot write:

${customer.name = "John"} ❌

Because ${} is immediate evaluation → read-only.

🔹 2.2 Lvalue Expressions (Read + Write)

These use #{} and support deferred evaluation.

Example (JSF):

<h:inputText value="#{customer.name}" />

On initial render:

customer.getName();

On postback:

customer.setName(submittedValue);

This is why #{} can act as:

Rvalue during rendering

Lvalue during update model phase

🔶 PART 3 — How EL Resolves Variables

When you write:

${customer}

The container internally does:

pageContext.findAttribute("customer");

Search order:

Page scope

Request scope

Session scope

Application scope

If not found → returns null.

If the name matches an implicit object (like session, request, etc.), that implicit object wins.

🔶 PART 4 — Referencing Object Properties

Unified EL supports two notations:

Dot notation
${customer.name}
Bracket notation
${customer["name"]}

Both resolve to:

customer.getName();

You can combine:

${customer.address["street"]}

Equivalent to:

customer.getAddress().getStreet();
🔶 PART 5 — Working With Collections
Lists / Arrays
${customer.orders[1]}

Internally:

customer.getOrders().get(1);

You must use integer index.

Maps
${customer.orders["socks"]}

Internally:

customer.getOrders().get("socks");

No integer coercion needed.

🔶 PART 6 — Enum Handling

Example enum:

public enum Suit {
    hearts, spades, diamonds, clubs
}

Expression:

${mySuit == "hearts"}

Internally EL does:

Suit.valueOf("hearts");

Then compares.

This automatic coercion is built into EL.

🔶 PART 7 — Supported Literals

Unified EL supports:

true
false
null
57
3.14
"string"
'string'

Examples:

${true}
${57}
${customer.age + 20}
${"Hello"}
🔶 PART 8 — Composite Expressions

You can mix text + expressions:

<some:tag value="Total: ${cart.total} USD"/>

Evaluation order:

Evaluate ${cart.total}

Convert to String

Concatenate with text

Coerce to expected attribute type

🔶 PART 9 — Type Conversion (Important)

EL automatically converts types.

Example:

If attribute expects float:

<some:tag value="${1.2E4}" />

Internally:

Float.valueOf("1.2E4").floatValue();

EL performs coercion automatically.

🔷 PART 10 — Method Expressions

Now we move to the second type.

A method expression represents a method to invoke later.

They always use #{} because invocation may happen later.

Example — JSF Form
<h:form>

    <h:inputText
        id="name"
        value="#{customer.name}"
        validator="#{customer.validateName}" />

    <h:commandButton
        id="submit"
        action="#{customer.submit}" />

</h:form>
What is this?
🔶 What Happens Internally?
1️⃣ Value Binding
value="#{customer.name}"

Used for:

getName()

setName()

2️⃣ Validator Method Expression
validator="#{customer.validateName}"

JSF expects signature like:

public void validateName(FacesContext context,
                         UIComponent component,
                         Object value)

JSF calls this during:

👉 Process Validation phase

3️⃣ Action Method Expression
action="#{customer.submit}"

Expected signature:

public String submit()

JSF calls this during:

👉 Invoke Application phase

The returned String determines navigation.

🔶 Why Method Expressions Must Be Deferred

Because invocation happens:

At different lifecycle phases

Not during initial rendering

Possibly multiple times

So ${} cannot work here.

🔶 Dot vs Bracket for Methods

These are equivalent:

#{customer.submit}
#{customer["submit"]}

EL resolves method name as string.

🔷 PART 11 — Where Method Expressions Can Be Used

Only:

In tag attributes

With single expression

Or literal

Example:

<some:tag value="#{bean.method}" />

The tag receives a MethodExpression object.

Later it invokes:

methodExpression.invoke(context, params);
🔷 PART 12 — Method Expressions vs EL Functions

Important distinction:

Method Expression

Calls method on managed bean:

#{customer.submit}
EL Function

Defined in tag library:

${fn:length(list)}

Functions:

Static methods

Evaluated immediately

Not lifecycle aware

Method expressions:

Deferred

Lifecycle aware

Can modify state