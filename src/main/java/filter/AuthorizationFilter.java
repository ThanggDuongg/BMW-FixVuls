package filter;


import dto.AccountDTO;
import util.SessionUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthorizationFilter implements Filter {

    private ServletContext context;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.context = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //Content Security Policy (CSP) Header Not Set
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse)response).setHeader("Content-Security-Policy", "default-src 'self'");
        }
        //Missing Anti-clickjacking Header
        response.addHeader("X-Frame-Options", "DENY");
        //X-Content-Type-Options Header Missing
        response.setHeader("X-Content-Type-Options", "nosniff");
        //Cookie without SameSite Attribute
        response.setHeader("Set-Cookie", "key=value; HttpOnly; SameSite=Strict");


        String url = request.getRequestURI();
        if (url.startsWith("/admin")) {
            AccountDTO model = (AccountDTO) SessionUtil.getInstance().getValue(request, "USERMODEL");
            if (model != null) {
                if (model.getRolename().equals("admin")) {
                    filterChain.doFilter(servletRequest, servletResponse);
                } else if (model.getRolename().equals("user")) {
                    response.sendRedirect(request.getContextPath()+"/view-login?action=login&message=not_permission&alert=danger");
                }
            } else {
                response.sendRedirect(request.getContextPath()+"/view-login?action=login&message=not_login&alert=danger");
            }
        }else if (url.startsWith("/user")){
            AccountDTO model = (AccountDTO) SessionUtil.getInstance().getValue(request, "USERMODEL");
            if (model != null) {
                if (model.getRolename().equals("user")) {
                    filterChain.doFilter(servletRequest, servletResponse);
                } else if (model.getRolename().equals("admin")) {
                    response.sendRedirect(request.getContextPath()+"/view-login?action=login&message=not_permission&alert=danger");
                }
            } else {
                response.sendRedirect(request.getContextPath()+"/view-login?action=login&message=not_login&alert=danger");
            }
        }
        else {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    @Override
    public void destroy() {

    }
}
