package controller.web;

import dao.IAccountDAO;
import dto.AccountDTO;
import entity.Account;
import service.IAccountService;
import util.CSRFUtil;
import util.FormUtil;
import util.SessionUtil;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = {"/view-home", "/view-login", "/view-logout"})
public class HomeController extends HttpServlet {


    @Inject
    private IAccountService accountService;

    ResourceBundle resourceBundle = ResourceBundle.getBundle("message");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (action != null && action.equals("login")) {
            AccountDTO accountDTO =(AccountDTO) SessionUtil.getInstance().getValue(request,"USERMODEL");
            if(accountDTO == null){
                String alert = request.getParameter("alert");
                String message = request.getParameter("message");
                if (message != null && alert != null) {
//                    request.setAttribute("message", resourceBundle.getString(message));
                    boolean validate_alert = alert.matches(".*[%<>&;'\0-].*");
                    boolean mess_val = message.matches(".*[%<>&;'\0-].*");
                    System.out.print(">> boolean: " + validate_alert);
                    if (validate_alert || mess_val) {
                        alert = "danger";
                        request.setAttribute("message", resourceBundle.getString("attack_xss"));
                    }
                    else {
                        request.setAttribute("message", resourceBundle.getString(message));
                    }
                    request.setAttribute("alert", alert);
                }
                RequestDispatcher rd = request.getRequestDispatcher("/home/login.jsp");
                rd.forward(request, response);
            }
            else {
                if (accountDTO.getRolename().equals("admin")) {
                    response.sendRedirect(request.getContextPath()+"/admin-home");
                } else if (accountDTO.getRolename().equals("user")) {
                    response.sendRedirect(request.getContextPath()+"/user-home");
                }
            }
        } else if (action != null && action.equals("logout")) {
            SessionUtil.getInstance().removeValue(request, "USERMODEL");
            SessionUtil.getInstance().removeValue(request, "CARTKEY");
            response.sendRedirect(request.getContextPath()+"/view-home");
        } else {
            RequestDispatcher rd = request.getRequestDispatcher("/home/home.jsp");
            rd.forward(request, response);
        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action != null && action.equals("login")) {
            if (!CSRFUtil.doAction(request, response)) {
                return;
            }
            String username = request.getParameter("username");
            String password = request.getParameter("pass");
            boolean user_val = username.matches(".*[%<>&;'\0-].*");
            boolean pass_val = password.matches(".*[%<>&;'\0-].*");
            if (user_val || pass_val) {
                return;
            }
            AccountDTO accountDTO = FormUtil.toModel(AccountDTO.class, request);
            accountDTO = accountService.findByUsernamePassword(accountDTO.getUsername(), accountDTO.getPass());
            AccountDTO ac = accountDTO;
            if (accountDTO != null && accountDTO.getActive()==true) {
                SessionUtil.getInstance().putValue(request, "USERMODEL", accountDTO);
                if (accountDTO.getRolename().equals("admin")) {
                    response.sendRedirect(request.getContextPath()+"/admin-home");
                } else if (accountDTO.getRolename().equals("user")) {
                    response.sendRedirect(request.getContextPath()+"/user-home");
                }
            } else {
//                RequestDispatcher rd = request.getRequestDispatcher("/home/home.jsp");
//                rd.forward(request, response);
                response.sendRedirect(request.getContextPath()+"/view-login?action=login&message=username_password_invalid&alert=danger");
            }
        }

    }
}