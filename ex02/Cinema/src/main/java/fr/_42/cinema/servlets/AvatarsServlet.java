package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import jakarta.servlet.ServletException;

@WebServlet("/avatars/*")
public class AvatarsServlet extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "File name missing");
            return;
        }

        String fileName = pathInfo; 
        if (fileName.contains("..")) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String storagePath = (String) getServletContext().getAttribute("storagePath");

        if (storagePath == null) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Storage path not configured");
            return;
        }

        File file = new File(storagePath, fileName);

        if (!file.exists() || !file.isFile()) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(file.getName());

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        res.setContentType(mimeType);
        res.setContentLengthLong(file.length());

        try (InputStream in = new FileInputStream(file);
             OutputStream out = res.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}