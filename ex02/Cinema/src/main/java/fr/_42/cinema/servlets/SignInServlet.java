package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.dao.*;
import jakarta.servlet.ServletException;
import fr._42.cinema.models.*;
import fr._42.cinema.services.*;
import fr._42.cinema.repositories.*;
import java.time.LocalDateTime;
import java.util.*;

@WebServlet("/signIn")
public class SignInServlet extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        req.getRequestDispatcher("/WEB-INF/html/signIn.html").forward(req, res);
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

            HistoryRepositoryImpl historyService = (HistoryRepositoryImpl) getServletContext().getAttribute("historyService");
            ImagesRepositoryImpl imagesService = (ImagesRepositoryImpl) getServletContext().getAttribute("imagesService");
            if (historyService == null || imagesService  == null) 
            {
                res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service not initialized.");
                return;
            }

            History history = new History(0L, user.getId(), req.getRemoteAddr(), LocalDateTime.now());

            historyService.save(history);

            List<History> historyList = historyService.findAllByUserId(user.getId());
            List<Image> imageList = imagesService.findAllByUserId(user.getId());

            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setAttribute("authentications", historyList);
            session.setAttribute("images", imageList);
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