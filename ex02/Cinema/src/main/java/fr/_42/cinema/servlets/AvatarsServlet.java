package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
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

        String fileName = pathInfo.substring(1); 

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

        
    }
}