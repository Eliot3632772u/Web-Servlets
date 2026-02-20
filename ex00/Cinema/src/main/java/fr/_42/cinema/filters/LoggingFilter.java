package fr._42.cinema.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebFilter;
import fr._42.cinema.models.*;
import java.io.*;

@WebFilter("/signIn")
public class LoggingFilter implements Filter {

    public void init(FilterConfig filterConfig) {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException 
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        
        if (user != null)
        {
            res.sendRedirect("/profile");
            return ;
        }
        chain.doFilter(req, res);
    }

    public void destroy() {
    }
}