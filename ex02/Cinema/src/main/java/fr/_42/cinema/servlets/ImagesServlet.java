package fr._42.cinema.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.dao.*;
import jakarta.servlet.ServletException;
import fr._42.cinema.models.*;
import fr._42.cinema.services.*;
import fr._42.cinema.repositories.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.UUID;
import java.io.File;
import org.springframework.dao.*;

@WebServlet("/images")
@MultipartConfig(location = "/tmp/uploads", maxFileSize = 1024 * 1024 * 5)
public class ImagesServlet extends HttpServlet
{
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        Part part = req.getPart("avatar");
        
        if (part != null && part.getSize() > 0) 
        {
            String fileName = part.getSubmittedFileName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            InputStream fileContent = part.getInputStream();
            BufferedImage image = ImageIO.read(fileContent);
            String storagePath = (String) getServletContext().getAttribute("storagePath");
            
            if (image != null) 
            {
                String randomFileName = UUID.randomUUID().toString() + "." + extension;
                ImageIO.write(image, extension, new File(storagePath + "/" + randomFileName));
                ImagesRepositoryImpl imagesRepo = (ImagesRepositoryImpl) getServletContext().getAttribute("imagesService");
                if (imagesRepo == null) 
                {
                    res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service not initialized.");
                    return;
                }

                try { 

                    HttpSession session = req.getSession(false);
                    User user = (session != null) ? (User) session.getAttribute("user") : null;
                    if (user == null) 
                    {
                        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Log In First!");
                        return;
                    }
                    imagesRepo.save(new Image(0L, fileName, storagePath + "/" + randomFileName, user.getId(), "" + part.getSize()));
                    res.sendRedirect("/profile");

                } catch (DataAccessException e) {
                    e.printStackTrace();
                    res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred.");
                }
            } 
            else {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Uploaded file is not a valid image.");
                return;
            }
        }

        res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No Avatar uploaded.");
    }
}