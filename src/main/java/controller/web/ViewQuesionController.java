package controller.web;

import dto.AccountDTO;
import dto.PreviewDTO;
import dto.QuestionDTO;
import dto.ScoreDTO;
import entity.Question;
import paging.PageRequest;
import paging.Pageble;
import service.IExamService;
import service.IPreviewService;
import service.IQuestionService;
import service.IScoreService;
import sort.Sorter;
import util.CSRFUtil;
import util.FormUtil;
import util.MessageUtil;
import util.SessionUtil;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ViewQuesionController", value = "/user-view-question")
public class ViewQuesionController extends HttpServlet {
    @Inject
    private IQuestionService questionService;

    @Inject
    private IScoreService iScoreService;

    @Inject
    private IPreviewService iPreviewService;

    @Inject
    private IExamService iExamService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (!CSRFUtil.doAction(request, response)) {
            return;
        }
        String examID = request.getParameter("examID");
        String type = request.getParameter("type");
        String page = request.getParameter("page");
        String maxPageItem = request.getParameter("maxPageItem");
        String sortName = request.getParameter("sortName");
        String sortBy = request.getParameter("sortBy");
        String csrf = request.getParameter("csrf");
        if (examID.length() > 10 || type.length() > 20 || page.length() > 10 || maxPageItem.length() > 10 ||
        sortName.length() > 20 || sortBy.length() > 10 || csrf.length() > 27) {
            return;
        }
        boolean examID_val = examID.matches(".*[%<>&;'\0-].*");
        boolean type_val = type.matches(".*[%<>&;'\0-].*");
        boolean page_val = page.matches(".*[%<>&;'\0-].*");
        boolean maxPageItem_val = maxPageItem.matches(".*[%<>&;'\0-].*");
        boolean sortName_val = sortName.matches(".*[%<>&;'\0-].*");
        boolean sortBy_val = sortBy.matches(".*[%<>&;'\0-].*");
        boolean csrf_val = csrf.matches(".*[%<>&;'\0-].*");

        if (examID_val || type_val || page_val || maxPageItem_val || sortName_val || sortBy_val || csrf_val) {
//            RequestDispatcher rd = request.getRequestDispatcher("/home/login.jsp");
//            rd.forward(request, response);
            return;
        }

        if (SessionUtil.getInstance().getValue(request, "LISTQUESTION") != null) {
            SessionUtil.getInstance().removeValue(request, "LISTQUESTION");
            SessionUtil.getInstance().removeValue(request, "LISTCURRENTQUESTION");
        }
        QuestionDTO questionDTO = FormUtil.toModel(QuestionDTO.class, request);

        Pageble pageble = new PageRequest(questionDTO.getPage(), questionDTO.getMaxPageItem(),
                new Sorter(questionDTO.getSortName(), questionDTO.getSortBy()));

        List<QuestionDTO> questionDTOCurrentList = questionService.findAll(pageble, questionDTO.getExamID());
        if(questionDTOCurrentList == null){
            String courseId = iExamService.findById(questionDTO.getExamID()).getCourseID().toString();
            response.sendRedirect(request.getContextPath() + "/user-exam?courseId="+courseId);
        }
        else{
            questionDTO.setListResult(questionDTOCurrentList);
            questionDTO.setTotalItem(questionService.getTotalItem(questionDTO.getExamID()));
            questionDTO.setTotalPage((int) Math.ceil((double) questionDTO.getTotalItem() / questionDTO.getMaxPageItem()));
            Integer userId = ((AccountDTO) SessionUtil.getInstance().getValue(request, "USERMODEL")).getId();

            if (questionDTO.getType().equals("preview")) {
                String courseId = iExamService.findById(questionDTO.getExamID()).getCourseID().toString();
                List<PreviewDTO> previewDTOS = iPreviewService.findByUserId(userId);
                questionService.setListPreview(questionDTO, previewDTOS);
                request.setAttribute("courseId", courseId);
                request.setAttribute("question", questionDTO);
                RequestDispatcher rd = request.getRequestDispatcher("/user/Preview.jsp");
                rd.forward(request, response);
            } else {

                ScoreDTO scoreDTO = iScoreService.findByExamIdAndUserId(questionDTO.getExamID(),userId);

                if (scoreDTO != null){
                    if (iScoreService.deleteScore(scoreDTO) && iPreviewService.deleteListPreviewByExamIdAndUserId(questionDTO.getExamID(),userId)){

                    }
                }

                List<QuestionDTO> questionDTOList = questionService.getListQuestionDTOByExamId(questionDTO.getExamID());
                SessionUtil.getInstance().putValue(request, "LISTQUESTION", questionDTOList);
                Integer courseId = iExamService.getCourseIdByExamId(questionDTOList.get(0).getExamID());
                SessionUtil.getInstance().putValue(request, "LISTCURRENTQUESTION", questionDTOCurrentList);
                request.setAttribute("courseId", courseId);
                request.setAttribute("question", questionDTO);
                RequestDispatcher rd = request.getRequestDispatcher("/user/practise.jsp");
                rd.forward(request, response);
            }
        }


//        MessageUtil.showMessage(request);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (!CSRFUtil.doAction(request, response)) {
            return;
        }
        List<QuestionDTO> questionDTOList = (List<QuestionDTO>) SessionUtil.getInstance().getValue(request, "LISTQUESTION");
        List<QuestionDTO> questionDTOOldList = (List<QuestionDTO>) SessionUtil.getInstance().getValue(request, "LISTCURRENTQUESTION");

        questionDTOList = questionService.setListAnswer(questionDTOList, questionDTOOldList, request);
        QuestionDTO questionDTO = FormUtil.toModel(QuestionDTO.class, request);
        String submit = request.getParameter("submittype");

        Integer courseId = iExamService.getCourseIdByExamId(questionDTO.getExamID());

        if (submit.equals("")) {
            double score = iScoreService.calculateScore(questionDTOList);
            Integer userId = ((AccountDTO) SessionUtil.getInstance().getValue(request, "USERMODEL")).getId();
            Integer examId = questionDTO.getExamID();

            ScoreDTO scoreDTO = new ScoreDTO();
            scoreDTO.setUserId(userId);
            scoreDTO.setExamId(examId);
            scoreDTO.setExamScore(score);

            if (iScoreService.insertScore(scoreDTO)) {
                if (iPreviewService.insertListPreviewWithQuestion(questionDTOList, userId)) {

                    SessionUtil.getInstance().removeValue(request, "LISTQUESTION");
                    SessionUtil.getInstance().removeValue(request, "LISTCURRENTQUESTION");
                    String url = "/user-exam?courseId=" + courseId.toString();
                    response.sendRedirect(request.getContextPath() + url);
                }

            }
        } else {
            Pageble pageble = new PageRequest(questionDTO.getPage(), questionDTO.getMaxPageItem(),
                    new Sorter(questionDTO.getSortName(), questionDTO.getSortBy()));

            List<QuestionDTO> questionDTOCurrentList = questionService.findAll(pageble, questionDTO.getExamID());
            questionDTO.setListResult(questionDTOCurrentList);
            questionDTO.setTotalItem(questionService.getTotalItem(questionDTO.getExamID()));
            questionDTO.setTotalPage((int) Math.ceil((double) questionDTO.getTotalItem() / questionDTO.getMaxPageItem()));

            SessionUtil.getInstance().putValue(request, "LISTCURRENTQUESTION", questionDTOCurrentList);
            SessionUtil.getInstance().putValue(request, "LISTQUESTION", questionDTOList);

            request.setAttribute("courseId", courseId);
            request.setAttribute("question", questionDTO);
            RequestDispatcher rd = request.getRequestDispatcher("/user/practise.jsp");
            rd.forward(request, response);
        }

    }
}
