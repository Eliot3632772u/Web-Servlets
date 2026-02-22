<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Profile</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f6f9;
            margin: 0;
            padding: 0;
        }

        .container {
            width: 800px;
            margin: 40px auto;
        }

        h1 {
            text-align: center;
            color: #333;
        }

        .card {
            background: white;
            padding: 20px;
            margin-bottom: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
        }

        .card h2 {
            margin-top: 0;
            color: #444;
            border-bottom: 1px solid #eee;
            padding-bottom: 10px;
        }

        .user-info p {
            margin: 8px 0;
        }

        .user-info strong {
            display: inline-block;
            width: 120px;
            color: #555;
        }

        ul {
            list-style: none;
            padding: 0;
        }

        li {
            padding: 8px 0;
            border-bottom: 1px solid #eee;
        }

        li:last-child {
            border-bottom: none;
        }

        a {
            text-decoration: none;
            color: #2c7be5;
        }

        a:hover {
            text-decoration: underline;
        }

        .upload-form input[type="file"] {
            margin-bottom: 10px;
        }

        .upload-form button {
            padding: 8px 16px;
            border: none;
            background-color: #2c7be5;
            color: white;
            border-radius: 4px;
            cursor: pointer;
        }

        .upload-form button:hover {
            background-color: #1a5fd1;
        }

        .empty-message {
            color: #777;
            font-style: italic;
        }
    </style>
</head>
<body>

<div class="container">

    <h1>Profile</h1>

    <!-- USER INFO -->
    <div class="card">
        <h2>User Information</h2>
        <div class="user-info">
            <p><strong>First Name:</strong> <c:out value="${user.firstName}" /></p>
            <p><strong>Last Name:</strong> <c:out value="${user.lastName}" /></p>
            <p><strong>Email:</strong> <c:out value="${user.email}" /></p>
        </div>
    </div>

    <!-- AUTHENTICATION HISTORY -->
    <div class="card">
        <h2>Authentication History</h2>

        <c:if test="${empty authentications}">
            <p class="empty-message">No authentication records found.</p>
        </c:if>

        <c:if test="${not empty authentications}">
            <ul>
                <c:forEach var="auth" items="${authentications}">
                    <li>
                        <strong>Date:</strong>
                        <c:out value="${auth.loginAt}" />
                        |
                        <strong>IP:</strong>
                        <c:out value="${auth.ipAddress}" />
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </div>

    <!-- UPLOAD AVATAR -->
    <div class="card">
        <h2>Upload Avatar</h2>

        <form action="${pageContext.request.contextPath}/images"
              method="post"
              enctype="multipart/form-data"
              class="upload-form">

            <input type="file" name="avatar" required />
            <br>
            <button type="submit">Upload</button>

        </form>
    </div>

    <!-- UPLOADED IMAGES -->
    <div class="card">
        <h2>Uploaded Images</h2>

        <c:if test="${empty images}">
            <p class="empty-message">No images uploaded.</p>
        </c:if>

        <c:if test="${not empty images}">
            <ul>
                <c:forEach var="image" items="${images}">
                    <li>
                        <a href="${pageContext.request.contextPath}/avatars/${image.filepath}"
                           target="_blank">
                            <c:out value="${image.filename}" />
                        </a>
                        —
                        <span><c:out value="${image.fileSize}" /></span>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </div>

</div>

</body>
</html>