package controller.admin;

import converter.QuestionConverter;
import dto.ExamDTO;
import dto.QuestionDTO;
import entity.Exam;
import entity.Question;
import service.IQuestionService;
import service.impl.ExamService;
import util.CSRFUtil;
import util.FormUtil;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


@MultipartConfig
@WebServlet(name = "QuestionController", value = "/QuestionController")
public class QuestionController extends HttpServlet {
    @Inject
    private IQuestionService questionService;
    @Inject
    private ExamService examService;

    private QuestionConverter questionConverter = new QuestionConverter();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if (!CSRFUtil.doAction(request, response)) {
            return;
        }
        if(action.equals("insertFile")){
            String idd = request.getParameter("id");
            String courIdd = request.getParameter("courseID");
            String examName = request.getParameter("examName");
            String fileCheck = request.getParameter("fileCheck");
            boolean val_exam = examName.matches(".*[%<>&;'\0-].*");
            boolean val_file = fileCheck.matches(".*[%<>&;'\0-].*");
//            System.out.print(">>> check: " + val_al);
            if (examName.length() > 20 || fileCheck.length() > 30 || idd.length() > 20 || courIdd.length() > 20) {
                return;
            }
            if (val_exam || val_file) {
//                resp.sendRedirect(req.getContextPath()+"/profile-admin?message=attack_xss&alert=error");
                return;
            }
            int id = Integer.parseInt(idd);
            int courseID = Integer.parseInt(courIdd);
            ExamDTO examDTO = new ExamDTO();
            examDTO.setId(id);
            examDTO.setExamName(examName);
            examDTO.setCourseID(courseID);
            examDTO.setFileCheck(fileCheck);
            try{

                Part part = request.getPart("file-exam");
                String realPath = request.getServletContext().getRealPath("/fileupload");
                String filename = Paths.get(part.getSubmittedFileName()).getFileName().toString();

                if(!Files.exists(Paths.get(realPath))){
                    Files.createDirectories(Paths.get(realPath));
                }
                String address = realPath + "/"+filename;
                part.write(address);

                String msg = questionService.importExcelXLSX(address, examDTO);
                if(msg != null){
                    Exam exam = examService.updateExam(examDTO);
                    response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=insert_success");
                }
                else{
                    response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=error_system");
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            if (action.equals("deleteFile")){
                ExamDTO examDTO = FormUtil.toModel(ExamDTO.class, request);
                int examId = examDTO.getId();
                List<Question> list = questionService.getListQuestionByExamId(examId);

                int length = list.size();
                int count = 0;
                for (Question question : list){
                    QuestionDTO questionDTO = questionConverter.toDto(question);
                    questionService.deleteQuestion(questionDTO);
                    count = count + 1;
                }
                if (count == length){
                    if (examService.deleteExam(examDTO)){
                        response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=delete_success");
                    }
                    else{
                        response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=error_system");
                    }
                }
                else{
                    response.sendRedirect(request.getContextPath()+"/admin-manager-file?message=error_system");
                }
            }
        }
    }
}
