package com.example.project.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.project.entity.Comment;
import com.example.project.entity.Member;
import com.example.project.entity.QnA;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.MemberRepository;
import com.example.project.repository.QnARepository;

@Controller
@RequestMapping("/category")
public class QnAController {

    @Autowired
    HttpSession session;

    @Autowired
    QnARepository qnaRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    CommentRepository commentRepository;

    @PostMapping("/write")
    @ResponseBody
    public Map<String, Object> write(@RequestBody Map<String, Object> data) {
        // Map<String, Object>
        Map<String, Object> response = new HashMap<>();
        String title = (String) data.get("title");
        String content = (String) data.get("content");
        String subject = (String) data.get("subject");
        String userId = (String) data.get("id");
        Integer id = (Integer) data.get("pid");

     
        // if (userId==null) {
        // response.put("success", false);
        // response.put("errorMessage", "로그인 해주세요.");
        // System.out.println(response);
        // return response;
        // }

        if (subject.trim().equals("")) {
            response.put("success", false);
            response.put("errorMessage", "문의종류가 선택되지 않았습니다.");
            return response;
        }
        if (title.trim().equals("")) {
            response.put("success", false);
            response.put("errorMessage", "제목이 없습니다.");
            return response;
        }
        if (content.trim().equals("")) {
            response.put("success", false);
            response.put("errorMessage", "게시물 내용이 없습니다.");
            return response;
        }
        

        try {
            Optional<Member> isMember = memberRepository.findById(userId);
            QnA qna = new QnA();
            if( id!=null){//수정일경우
                
                qna = qnaRepository.findById(id).get();
                if(isMember==null || !userId.equals(qna.getMember().getId())){

                     response.put("success", false);
                    response.put("errorMessage", "권한이 없습니다.");
                    
                                                
                    return response;
                }

                
                // qna.setId(Integer.valueOf(id));
                qna.setModifiedDate(new Date());

            }else{                                                

                
            qna.setCreatedDate(new Date());

            }
            
            qna.setSubject(subject);
            qna.setTitle(title);
            qna.setContent(content);
            Member member = new Member();
            member.setId(userId);
            qna.setMember(member);
         
            qnaRepository.save(qna);

            response.put("success", true);
            response.put("successMessage", "작성을 완료 했습니다.");

            return response;

        } catch (Exception e) {

            response.put("error", "글작성 중 오류가 발생하였습니다.");
            return response;
        }

    }

    @GetMapping("/qnaList")
    @ResponseBody
    public Map<String, Object>  getList(@RequestParam(required = false, defaultValue = "1") Integer p){
        
        Map<String, Object> map = new HashMap<String, Object>();

        Sort sort = Sort.by(Direction.DESC,"id");



        Pageable page = PageRequest.of(p-1, 10,sort );//기본 페이지는 1페이지지만 페이저블은 0이 처음페이지라서
        Page<QnA> QnAList = qnaRepository.findAll(page);
        List<QnA> total = qnaRepository.findAll();
        map.put("QnAList", QnAList);
        map.put("totalElements", total.size());

        return map;

    }


    @GetMapping("/readQnA")
    @ResponseBody
    public Map<String, Object>  getReadQnA(@RequestParam(required = false) Integer pid){
         Map<String, Object> response = new HashMap<>();
        if (pid ==null){
            response.put("message", "page_error");
            return response;
        }
      
        Optional<QnA> readQnA = qnaRepository.findById(pid);
        if(readQnA.isEmpty()){
            response.put("message", "page_error");
            return response; 
        }
        response.put("massage", "success");
        response.put("data", readQnA);
        

        return response;

    }

    @PostMapping("/deleteQnA")
    @ResponseBody
      public Map<String, Object> deleteQnA(@RequestBody Map<String, Object> data){
         Map<String, Object> response = new HashMap<>();


       try {
            Integer pid = (Integer) data.get("pid");
            String id = (String) data.get("id");

            Optional<Member> loginId = memberRepository.findById(id);
    
            if (!loginId.isPresent()) {
                response.put("message", "login_error");
            }            
            else if (pid == null) {

                response.put("message", "delete_error");
            }
             else {
                Optional<QnA> findQnA = qnaRepository.findById(pid);
                if (findQnA.isEmpty()) {

                    response.put("message", "not_find_error");
                }
                
                else if(!findQnA.get().getMember().getId().equals(id)){
                     response.put("message", "authority_error");

                }
                
                else {
                    qnaRepository.delete(findQnA.get());
                    response.put("message", "success");

                }
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            response.put("message", "error");
            e.printStackTrace(); // 예외 정보 출력 (실제 서비스에서는 로그로 기록해야 함)
        }
        return response;
        
    }


    @PostMapping("/writeComment")
    @ResponseBody
    public Map<String, Object> writeComment(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
   
        String content = (String) data.get("content");
        Integer pid = (Integer) data.get("pid");
        String userId = (String) data.get("id");
        Integer cid = (Integer) data.get("cid");
     
        // if (session.getAttribute("id")==null) {
        // response.put("message", "로그인 해주세요.");
        // System.out.println(response);
        // return response;
        // }세션에서 로그인이 안되었을 경우

        if (content.trim().equals("")) {
            response.put("message", "content_error");
            return response;
        }
        else if (pid==null) {
            response.put("message", "page_error");
            return response;
        }

        else if (userId ==null){
            response.put("message", "login_error");
            return response;
        }

        

        try {

            Optional<Member> isMember = memberRepository.findById(userId);
            Comment comment = new Comment();
            if( cid!=null){
                comment = commentRepository.findById(cid).get();
                // qna.setId(Integer.valueOf(id));
                comment.setModifiedDate(new Date());

            }else{
            comment.setCreatedDate(new Date());

            }
            comment.setContent(content);
            QnA qna = new QnA();
            qna.setId(pid);
            comment.setQna(qna);

            // Member member = new Member();
            // member.setId();
            comment.setWriter(isMember.get().getId());
            commentRepository.save(comment);
            response.put("success", true);
            response.put("successMessage", "작성을 완료 했습니다.");
            return response;

        } catch (Exception e) {
            response.put("error", "글작성 중 오류가 발생하였습니다.");
            return response;
        }

    }



    
  @PostMapping("/deleteComment")
    @ResponseBody
      public Map<String, Object> deleteComment(@RequestBody Map<String, Object> data){
         Map<String, Object> response = new HashMap<>();


       try {
            Integer pid = (Integer) data.get("pid");
            String id = (String) data.get("id");
            Integer cid = (Integer) data.get("cid");

            // session.setAttribute("id", "1");
            // String loginId = (String) session.getAttribute("id");// 세션 문제 수정 할것
    
            Optional<Member> loginId = memberRepository.findById(id);
            if (!loginId.isPresent()) {
                response.put("message", "login_error");
            }
            
            // else if(!id.equals(loginId)){
            //     response.put("message", "user_info_error");
            //                     System.out.println(2);

            // }
            
            else if (pid == null) {

                response.put("message", "delete_error");
            }
             else {
                Optional<QnA> findQnA = qnaRepository.findById(pid);
                Optional<Comment> findComment= commentRepository.findById(cid); 
                if (findQnA.isEmpty() || findComment.isEmpty()) {

                    response.put("message", "not_find_error");
                }
                else if(!findComment.get().getWriter().equals(id)){
                     response.put("message", "authority_error");

                }
                
                else {
                    commentRepository.delete(findComment.get());
                    response.put("message", "success");

                }
            }
        } catch (Exception e) {
            // 예외 발생 시 처리
            response.put("message", "error");
            e.printStackTrace(); // 예외 정보 출력 (실제 서비스에서는 로그로 기록해야 함)
        }
        return response;
        
    }
}