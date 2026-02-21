package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import jakarta.servlet.ServletException;

@WebServlet("/hello")
public class Hello extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        req.getRequestDispatcher("/WEB-INF/jsp/hello.jsp").forward(req, res);
    }
}