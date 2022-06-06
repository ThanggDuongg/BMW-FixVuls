package controller.web;

import dto.AccountDTO;
import service.IAccountService;
import util.CSRFUtil;
import util.FormUtil;
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

@WebServlet(urlPatterns = "/profile-user")
public class ProfileUser extends HttpServlet {
    @Inject
    private IAccountService accountService;
    ResourceBundle resourceBundle = ResourceBundle.getBundle("message");


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        AccountDTO accountDTO = (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
        if (accountDTO != null && accountDTO.getRoleid() == 2) {
            String action = req.getParameter("action");
            String message = req.getParameter("message");
            String alert = req.getParameter("alert");

            if (action != null) {

            } else if (message != null && alert != null) {
                boolean val_al = alert.matches(".*[%<>&;'\0-].*");
                boolean val_mes = message.matches(".*[%<>&;'\0-].*");
                System.out.print(">>> check: " + val_al);
                if (val_al || val_mes) {
                    resp.sendRedirect(req.getContextPath()+"/profile-user?message=attack_xss&alert=error");
                    return;
                }
                System.out.println(resourceBundle.getString(message));
                System.out.println(alert);
                req.setAttribute("message", resourceBundle.getString(message));
                req.setAttribute("alert", alert);
                RequestDispatcher rd = req.getRequestDispatcher("/user/profile-student.jsp");
                rd.forward(req, resp);
            } else {
                RequestDispatcher rd = req.getRequestDispatcher("/user/profile-student.jsp");
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
            resp.sendRedirect(req.getContextPath()+"/profile-user?message=Absence_of_Anti-CSRF_Tokens&alert=error");
        }
        String alert = req.getParameter("alert");
        String message = req.getParameter("message");

        System.out.print(">>> check: " + alert);

        System.out.print(">>> check: " + message);
        if ((alert != null && !alert.isEmpty()) || (message != null && !message.isEmpty())) {
            boolean val_al = alert.matches(".*[%<>&;'\0-].*");
            boolean val_mes = alert.matches(".*[%<>&;'\0-].*");
            System.out.print(">>> check: " + val_al);
            if (val_al || val_mes) {
                resp.sendRedirect(req.getContextPath()+"/profile-user?message=attack_xss&alert=error");
            }
        }

        if (action != null && action.equals("update-avatar")) {
            AccountDTO accountDTO= (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
            String urlAvatar = req.getParameter("urlAvatar");
            accountDTO.setUrlAvatar(urlAvatar);
            if (accountService.updateAccount(accountDTO) != null) {
                resp.sendRedirect(req.getContextPath()+"/profile-user?message=changed_avatar_successfully&alert=success");
            } else {
                resp.sendRedirect(req.getContextPath()+"/profile-user?message=changed_avatar_failed&alert=error");
            }
        } else if (action != null &&  action.equals("update-info")) {
            AccountDTO accountDTO = (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
            System.out.println(accountDTO);

            String fullname = req.getParameter("fullname");
            String birthday = req.getParameter("birthday");
            String gender = req.getParameter("gender");
            String phone = req.getParameter("phone");
            String email = req.getParameter("email");

            if (fullname.length() > 20 || birthday.length() > 15 || gender.length() > 7 ||
            phone.length() > 20 || email.length() > 20) {
                resp.sendRedirect(req.getContextPath()+"/profile-user?message=buffer_overflow&alert=error");
                return;
            }

            accountDTO.setFullname(fullname);
            accountDTO.setBirthday(LocalDate.parse(birthday,
                    DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            accountDTO.setGender(gender);
            accountDTO.setPhone(phone);
            accountDTO.setEmail(email);
            if (accountService.updateAccount(accountDTO) != null) {
                resp.sendRedirect(req.getContextPath()+"/profile-user?message=changed_information_successfully&alert=success");
            } else {
                resp.sendRedirect(req.getContextPath()+"/profile-user?message=changed_information_failed&alert=error");
            }
        } else if (action != null && action.equals("update-password")) {
            AccountDTO accountDTO = (AccountDTO) SessionUtil.getInstance().getValue(req, "USERMODEL");
            accountDTO.setOldPass(req.getParameter("old-password"));
            accountDTO.setNewPass(req.getParameter("new-password"));
            accountDTO.setComfirmPass(req.getParameter("confirm-password"));
            if (accountDTO.getPass().equals(accountDTO.getOldPass())) {
                if (accountDTO.getComfirmPass().equals(accountDTO.getNewPass())) {
                    if (accountService.updatePassword(accountDTO) != null) {
                        resp.sendRedirect(req.getContextPath()+"/profile-user?message=changed_password_successfully&alert=success");
                    } else {
                        resp.sendRedirect(req.getContextPath()+"/profile-user?message=changed_password_failed&alert=error");
                    }
                } else {
                    resp.sendRedirect(req.getContextPath()+"/profile-user?message=incorrect_confirm_password&alert=error");
                }
            } else {
                resp.sendRedirect(req.getContextPath()+"/profile-user?message=incorrect_password&alert=error");
            }
        }
    }
}
