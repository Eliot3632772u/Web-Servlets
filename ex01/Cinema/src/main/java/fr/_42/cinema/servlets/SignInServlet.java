package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.dao.*;
import jakarta.servlet.ServletException;
import fr._42.cinema.models.*;
import fr._42.cinema.services.*;

@WebServlet("/signIn")
public class SignInServlet extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        // HttpSession session = req.getSession();
        // User user = (User) session.getAttribute("user");

        // if (user == null)
            req.getRequestDispatcher("/WEB-INF/html/signIn.html").forward(req, res);
        // else 
        //     res.sendRedirect("/profile");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");

        if (isInvalid(phone) || isInvalid(password)) 
        {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "All fields are required.");
            return;
        }

        UsersService usersService = (UsersService) getServletContext().getAttribute("usersService");

        if (usersService == null) 
        {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service not initialized.");
            return;
        }

        try {

            User user = usersService.signIn(phone, password);
            if (user == null)
            {
                res.sendRedirect("/signUp");
                return ;
            }

            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            res.sendRedirect("/profile");

        } catch (DataAccessException e) {
            e.printStackTrace();
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred.");
        }   
    }

    private boolean isInvalid(String param) 
    {
        return param == null || param.trim().isEmpty();
    }
}