<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Profile</title>
</head>
<body>

<h1>Profile</h1>

<hr>

<h2>User Information</h2>

<p>
    <strong>First Name:</strong>
    <c:out value="${user.firstName}" />
</p>

<p>
    <strong>Last Name:</strong>
    <c:out value="${user.lastName}" />
</p>

<p>
    <strong>Email:</strong>
    <c:out value="${user.email}" />
</p>

<hr>

<h2>Authentication History</h2>

<c:if test="${empty authentications}">
    <p>No authentication records found.</p>
</c:if>

<c:if test="${not empty authentications}">
    <ul>
        <c:forEach var="auth" items="${authentications}">
            <li>
                Date: <c:out value="${auth.loginAt}" />
                |
                IP: <c:out value="${auth.ipAddress}" />
            </li>
        </c:forEach>
    </ul>
</c:if>

<hr>

<h2>Upload Avatar</h2>

<form action="/images"
      method="post"
      enctype="multipart/form-data">

    <input type="file" name="avatar" required />
    <button type="submit">Upload</button>

</form>

<hr>

<h2>Uploaded Images</h2>

<c:if test="${empty images}">
    <p>No images uploaded.</p>
</c:if>

<c:if test="${not empty images}">
    <ul>
        <c:forEach var="image" items="${images}">
            <li>
                <a href="/avatars/${image.filepath}"
                   target="_blank">
                    <c:out value="${image.filename}" />
                </a>
            </li>
        </c:forEach>
    </ul>
</c:if>

</body>
</html>