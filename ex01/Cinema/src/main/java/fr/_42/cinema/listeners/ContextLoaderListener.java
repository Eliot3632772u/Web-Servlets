package fr._42.cinema.listeners;

import jakarta.servlet.ServletContextListener; 
import jakarta.servlet.ServletContextEvent; 
import jakarta.servlet.annotation.WebListener;
import org.springframework.context.annotation.*;
import org.springframework.context.*;
import fr._42.cinema.services.*;
import fr._42.cinema.config.*;

@WebListener
public class ContextLoaderListener implements ServletContextListener
{
    public void	contextInitialized(ServletContextEvent sce)
    {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        UsersService usersService = ctx.getBean(UsersService.class);
        sce.getServletContext().setAttribute("usersService", usersService);
    }

    public void contextDestroyed(ServletContextEvent sce)
    {
        sce.getServletContext().removeAttribute("usersService");
    }
}