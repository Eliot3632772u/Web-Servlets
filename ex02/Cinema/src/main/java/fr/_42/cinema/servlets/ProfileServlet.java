package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import jakarta.servlet.ServletException;
import fr._42.cinema.models.*;
import fr._42.cinema.repositories.*;
import java.util.*;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        HistoryRepositoryImpl historyService = (HistoryRepositoryImpl) getServletContext().getAttribute("historyService");
        ImagesRepositoryImpl imagesService = (ImagesRepositoryImpl) getServletContext().getAttribute("imagesService");

        if (historyService == null || imagesService  == null) 
        {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service not initialized.");
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("user");
        List<History> historyList = historyService.findAllByUserId(user.getId());
        List<Image> imageList = imagesService.findAllByUserId(user.getId());

        session.setAttribute("authentications", historyList);
        session.setAttribute("images", imageList);

        req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, res);
    }
}