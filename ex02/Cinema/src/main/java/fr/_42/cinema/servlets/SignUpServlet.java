package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.dao.*;
import jakarta.servlet.ServletException;
import fr._42.cinema.models.*;
import fr._42.cinema.services.*;

@WebServlet("/signUp")
public class SignUpServlet extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        req.getRequestDispatcher("/WEB-INF/html/signUp.html").forward(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");
        String email = req.getParameter("email");

        if (isInvalid(firstName) || isInvalid(lastName) || isInvalid(phone) || isInvalid(password) || isInvalid(email)) 
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

            usersService.signUp(firstName, lastName, phone, password, email);
            res.sendRedirect("/signIn");

        } catch (DuplicateKeyException e) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
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


