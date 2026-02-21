package fr._42.cinema.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebFilter;
import fr._42.cinema.models.*;
import java.io.*;

@WebFilter("/profile")
public class ProfileFilter implements Filter {

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
            chain.doFilter(req, res);
            return ;
        }
        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Logging First!");
    }

    public void destroy() {
    }
}