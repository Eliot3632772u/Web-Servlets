# Exercise 02 – JSP

## Overview

Extends [Exercise 01](../ex01/) by replacing the blank profile page with a full JSP-based profile view. Adds authentication history tracking, avatar upload, and image serving. All authorization filters from ex01 remain unchanged.

---

## Changes from Exercise 01

### New Models

**`History`** — records each login event:
```java
public class History {
    private Long id;
    private Long userId;
    private String ipAddress;
    private LocalDateTime loginAt;
}
```

**`Image`** — records each uploaded file:
```java
public class Image {
    private Long id;
    private String filename;   // original file name shown to user
    private String filepath;   // UUID-based name stored on disk
    private Long userId;
    private String fileSize;   // human-readable e.g. "1.23 MB"
}
```

**`User`** — `email` field added:
```java
public class User {
    // ... existing fields
    private String email;
}
```

---

### New Repositories

**`HistoryRepositoryImpl`** — persists and queries login records via `JdbcTemplate`:
```java
public void save(History entity) {
    jdbcTemplate.update(
        "INSERT INTO history (user_id, ip_address, login_at) VALUES (?, ?, ?)",
        entity.getUserId(),
        entity.getIpAddress(),
        Timestamp.valueOf(entity.getLoginAt())
    );
}

public List<History> findAllByUserId(Long userId) {
    return jdbcTemplate.query(
        "SELECT * FROM history WHERE user_id = ?", mapper, userId);
}
```

**`ImagesRepositoryImpl`** — persists and queries uploaded image metadata:
```java
public void save(Image entity) {
    jdbcTemplate.update(
        "INSERT INTO images (file_name, file_path, user_id, file_size) VALUES (?, ?, ?, ?)",
        entity.getFilename(),
        entity.getFilepath(),
        entity.getUserId(),
        entity.getFileSize()
    );
}

public List<Image> findAllByUserId(Long userId) {
    return jdbcTemplate.query(
        "SELECT * FROM images WHERE user_id = ?", mapper, userId);
}
```

---

### `ApplicationConfig` — storage path bean

A `storagePath` bean reads the upload directory from `application.properties`:
```java
@Bean
public String storagePath() {
    return env.getProperty("storage.path");
}
```
```properties
# application.properties
storage.path=/tmp/uploads
```

---

### `ContextLoaderListener` — registers new services

The new repositories and storage path are retrieved from the Spring context and stored in `ServletContext`:
```java
HistoryRepositoryImpl historyService = ctx.getBean(HistoryRepositoryImpl.class);
ImagesRepositoryImpl imagesService   = ctx.getBean(ImagesRepositoryImpl.class);
String storagePath                   = ctx.getBean("storagePath", String.class);

sce.getServletContext().setAttribute("historyService", historyService);
sce.getServletContext().setAttribute("imagesService",  imagesService);
sce.getServletContext().setAttribute("storagePath",    storagePath);
```

---

### `SignInServlet` — records login history on success

After a successful authentication, a `History` record is saved with the user's ID, IP address, and current timestamp before the session is created:
```java
History history = new History(0L, user.getId(), req.getRemoteAddr(), LocalDateTime.now());
historyService.save(history);

List<History> historyList = historyService.findAllByUserId(user.getId());
List<Image>   imageList   = imagesService.findAllByUserId(user.getId());

HttpSession session = req.getSession();
session.setAttribute("user",            user);
session.setAttribute("authentications", historyList);
session.setAttribute("images",          imageList);
res.sendRedirect("/profile");
```

---

### `ProfileServlet` — forwards to JSP

Instead of returning a blank page, the servlet loads the user's history and images then forwards to the JSP view:
```java
List<History> historyList = historyService.findAllByUserId(user.getId());
List<Image>   imageList   = imagesService.findAllByUserId(user.getId());

session.setAttribute("authentications", historyList);
session.setAttribute("images",          imageList);

req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, res);
```

---

### `ImagesServlet` — handles avatar upload (`POST /images`)

Validates that the upload is a real image using `ImageIO.read()`, generates a UUID-based filename to guarantee uniqueness on disk, writes the file to `storagePath`, and saves the metadata to the database:
```java
@WebServlet("/images")
@MultipartConfig(maxFileSize = 1024 * 1024 * 5)
public class ImagesServlet extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        Part part = req.getPart("avatar");
        String fileName  = part.getSubmittedFileName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        BufferedImage image = ImageIO.read(part.getInputStream());
        if (image == null) {
            res.sendError(SC_BAD_REQUEST, "Not a valid image.");
            return;
        }

        String randomFileName = UUID.randomUUID().toString() + "." + extension;
        part.write(storagePath + "/" + randomFileName);

        imagesRepo.save(new Image(0L, fileName, randomFileName, user.getId(), formatFileSize(part.getSize())));
        res.sendRedirect("/profile");
    }
}
```

File sizes are formatted into a human-readable string (B / KB / MB / GB) by a private `formatFileSize` helper.

---

### `AvatarsServlet` — serves uploaded files (`GET /avatars/{filename}`)

Reads the file from `storagePath` and streams it back to the browser with the correct MIME type. Path traversal is blocked by rejecting any filename containing `..`:
```java
@WebServlet("/avatars/*")
public class AvatarsServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        String fileName = req.getPathInfo();
        if (fileName.contains("..")) {
            res.sendError(SC_FORBIDDEN);
            return;
        }

        File file = new File(storagePath, fileName);
        res.setContentType(getServletContext().getMimeType(file.getName()));
        res.setContentLengthLong(file.length());

        // stream file bytes to response output stream
    }
}
```

---

### `profile.jsp` — JSP view with JSTL

The profile page uses JSTL `<c:forEach>` to render authentication history and uploaded images, and a standard multipart form to upload an avatar:
```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Authentication history -->
<c:forEach var="auth" items="${authentications}">
    <li>
        <strong>Date:</strong> <c:out value="${auth.loginAt}" /> |
        <strong>IP:</strong>   <c:out value="${auth.ipAddress}" />
    </li>
</c:forEach>

<!-- Uploaded images list -->
<c:forEach var="image" items="${images}">
    <li>
        <a href="${pageContext.request.contextPath}/avatars/${image.filepath}" target="_blank">
            <c:out value="${image.filename}" />
        </a>
        — <c:out value="${image.fileSize}" />
    </li>
</c:forEach>

<!-- Upload form -->
<form action="/images" method="post" enctype="multipart/form-data">
    <input type="file" name="avatar" required />
    <button type="submit">Upload</button>
</form>
```

---

### Database Schema
```sql
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    first_name TEXT NOT NULL,
    last_name  TEXT NOT NULL,
    phone      TEXT UNIQUE NOT NULL,
    password   TEXT NOT NULL,
    email      TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS history (
    id         SERIAL PRIMARY KEY,
    user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ip_address TEXT NOT NULL,
    login_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS images (
    id        SERIAL PRIMARY KEY,
    user_id   INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    file_size TEXT NOT NULL
);
```

---

## Project Structure
```
Cinema/
├── src/main/java/fr/_42/cinema/
│   ├── config/          # ApplicationConfig — adds storagePath bean
│   ├── filters/         # LoggingFilter, ProfileFilter (unchanged)
│   ├── listeners/       # ContextLoaderListener — registers new services
│   ├── models/          # User (+ email), History, Image
│   ├── repositories/    # HistoryRepositoryImpl, ImagesRepositoryImpl, UsersRepositoryImpl
│   ├── services/        # UsersService, UsersServiceImpl (unchanged)
│   └── servlets/        # ProfileServlet, SignInServlet, ImagesServlet, AvatarsServlet
└── src/main/webapp/WEB-INF/
    ├── html/            # signIn.html, signUp.html (unchanged)
    └── jsp/
        └── profile.jsp  # JSP profile view
```

## Endpoints

| URL | Method | Description |
|---|---|---|
| `/signUp` | GET / POST | Registration (unchanged) |
| `/signIn` | GET / POST | Login — now also saves history |
| `/profile` | GET | JSP profile page |
| `/images` | POST | Upload avatar |
| `/avatars/{filename}` | GET | Serve uploaded image file |

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