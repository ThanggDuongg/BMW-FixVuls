package controller.admin;

import dto.AccountDTO;
import service.IAccountService;
import util.CSRFUtil;
import util.SessionUtil;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

@WebServlet(urlPatterns = "/admin-profile")
public class ProfileAdmin extends HttpServlet {
    @Inject
    private IAccountService accountService;
    ResourceBundle resourceBundle = ResourceBundle.getBundle("message");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        AccountDTO accountDTO = (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
        if (accountDTO != null && accountDTO.getRoleid() == 1) {
            String action = req.getParameter("action");
            String message = req.getParameter("message");
//            if (action.length() > 60 || message.length() > 60) {
//                return;
//            }
            String alert = req.getParameter("alert");

//            if (alert.length() > 60) {
//                return;
//            }

            if (action != null) {

            } else if (message != null && alert != null) {
                boolean val_al = alert.matches(".*[%<>&;'\0-].*");
                boolean val_mes = message.matches(".*[%<>&;'\0-].*");
                System.out.print(">>> check: " + val_al);
                if (val_al || val_mes) {
                    resp.sendRedirect(req.getContextPath()+"/profile-admin?message=attack_xss&alert=error");
                    return;
                }
                System.out.println(resourceBundle.getString(message));
                System.out.println(alert);
                req.setAttribute("message", resourceBundle.getString(message));
                req.setAttribute("alert", alert);
                RequestDispatcher rd = req.getRequestDispatcher("/admin/profile-admin.jsp");
                rd.forward(req, resp);
            } else {
                RequestDispatcher rd = req.getRequestDispatcher("/admin/profile-admin.jsp");
                rd.forward(req, resp);
            }
        } else {
            RequestDispatcher rd = req.getRequestDispatcher("/");
            rd.forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if (!CSRFUtil.doAction(req, resp)) {
            return;
        }
        if (action != null && action.equals("update-avatar")) {
            AccountDTO accountDTO= (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
            String urlAvatar = req.getParameter("urlAvatar");
            accountDTO.setUrlAvatar(urlAvatar);
            if (accountService.updateAccount(accountDTO) != null) {
                resp.sendRedirect(req.getContextPath()+"/admin-profile?message=changed_avatar_successfully&alert=success");
            } else {
                resp.sendRedirect(req.getContextPath()+"/admin-profile?message=changed_avatar_failed&alert=error");
            }
        } else if (action != null &&  action.equals("update-info")) {
            AccountDTO accountDTO = (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
            System.out.println(accountDTO);
            accountDTO.setFullname(req.getParameter("fullname"));
            accountDTO.setBirthday(LocalDate.parse(req.getParameter("birthday"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            accountDTO.setGender(req.getParameter("gender"));
            accountDTO.setPhone(req.getParameter("phone"));
            accountDTO.setEmail(req.getParameter("email"));
            if (accountService.updateAccount(accountDTO) != null) {
                resp.sendRedirect(req.getContextPath()+"/admin-profile?message=changed_information_successfully&alert=success");
            } else {
                resp.sendRedirect(req.getContextPath()+"/admin-profile?message=changed_information_failed&alert=error");
            }
        } else if (action != null && action.equals("update-password")) {
            AccountDTO accountDTO = (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
            accountDTO.setOldPass(req.getParameter("old-password"));
            accountDTO.setNewPass(req.getParameter("new-password"));
            accountDTO.setComfirmPass(req.getParameter("confirm-password"));
            if (accountDTO.getPass().equals(accountDTO.getOldPass())) {
                if (accountDTO.getComfirmPass().equals(accountDTO.getNewPass())) {
                    if (accountService.updatePassword(accountDTO) != null) {
                        resp.sendRedirect(req.getContextPath()+"/admin-profile?message=changed_password_successfully&alert=success");
                    } else {
                        resp.sendRedirect(req.getContextPath()+"/admin-profile?message=changed_password_failed&alert=error");
                    }
                } else {
                    resp.sendRedirect(req.getContextPath()+"/admin-profile?message=incorrect_confirm_password&alert=error");
                }
            } else {
                resp.sendRedirect(req.getContextPath()+"/admin-profile?message=incorrect_password&alert=error");
            }
        }
    }
}
