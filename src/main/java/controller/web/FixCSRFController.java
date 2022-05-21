package controller.web;

import util.CSRFUtil;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/fix-csrf"})
public class FixCSRFController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Boolean check = CSRFUtil.doAction(request, response);
        if (check) {
            response.sendRedirect(request.getContextPath()+"/admin-home");
        }
        else {
            return;
        }
    }
}
