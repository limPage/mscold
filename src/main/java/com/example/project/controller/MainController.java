package com.example.project.controller;

import java.util.HashMap;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.project.entity.Member;
import com.example.project.repository.MemberRepository;

@Controller
public class MainController {
    @Autowired
    HttpSession session;
    @Autowired
    MemberRepository memberRepository;

    @RequestMapping("/")
    public String main() {
        return "main";
    }

    @PostMapping("/SignIn")
    public ResponseEntity<Map<String, Object>> signInPost(@RequestBody Map<String, String> credentials) {
        String ID = credentials.get("ID");
        String PW = credentials.get("PW");

        Optional<Member> member = memberRepository.findByIdAndPw(ID, PW);
        // System.out.println("ID: " + ID + ", PW: " + PW);
        Map<String, Object> response = new HashMap<>();
        // System.out.println(member);
        if (member.isPresent()) {
            session.setAttribute("id", ID);
            response.put("success", true);
        } else {
            response.put("success", false);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/SignUp")
    @ResponseBody
    public Map<String, Object> signUp(@RequestBody Map<String, String> inputData) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Member> idIsPresent = memberRepository.findById(inputData.get("ID"));
        Optional<Member> contactIsPresent = memberRepository.findByContact(inputData.get("CONTACT"));
        Optional<Member> emailIsPresent = memberRepository.findByEmail(inputData.get("EMAIL"));

        System.out.println(inputData);

        if (idIsPresent.isPresent()) {
            response.put("success", false);
            response.put("errorMessage", "이미 사용중인 아이디입니다.");
            System.out.println(response);
            return response;
        }
        if (contactIsPresent.isPresent()) {
            response.put("success", false);
            response.put("errorMessage", "이미 사용중인 연락처입니다.");
            return response;
        }
        if (emailIsPresent.isPresent()) {
            response.put("success", false);
            response.put("errorMessage", "이미 사용중인 이메일입니다.");
            return response;
        }
        

        try {
            
            Member member = new Member();
            member.setId(inputData.get("ID"));
            member.setPw(inputData.get("PW"));
            member.setContact(inputData.get("CONTACT"));
            member.setEmail(inputData.get("EMAIL"));
            member.setAddress(inputData.get("ADDRESS"));
            memberRepository.save(member);

            response.put("success", true);
            response.put("successMessage", "회원가입에 성공하셨습니다!");
            return response;
        } catch (Exception e) {
            response.put("error", "회원 가입 중 오류가 발생하였습니다.");
            return response;
        }
    }

    @PostMapping("/EditMemberInfo")
    @ResponseBody
    public Map<String, Object> EditMemberInfo(@RequestBody Map<String, String> inputData) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Member> contactIsPresent = memberRepository.findByContact(inputData.get("CONTACT"));
        Optional<Member> emailIsPresent = memberRepository.findByEmail(inputData.get("EMAIL"));

        System.out.println(inputData);

        if (contactIsPresent.isPresent()) {
            response.put("success", false);
            response.put("errorMessage", "이미 사용중인 연락처입니다.");
            return response;
        }
        if (emailIsPresent.isPresent()) {
            response.put("success", false);
            response.put("errorMessage", "이미 사용중인 이메일입니다.");
            return response;
        }
        try {
            Member member = new Member();
            member.setId(inputData.get("ID"));
            member.setPw(inputData.get("PW"));
            member.setContact(inputData.get("CONTACT"));
            member.setEmail(inputData.get("EMAIL"));
            member.setAddress(inputData.get("ADDRESS"));
            memberRepository.save(member);
            response.put("success", true);
            response.put("successMessage", "개인정보수정 성공");
            return response;
        } catch (Exception e) {
            response.put("error", "개인정보수정 중 오류가 발생하였습니다.");
            return response;
        }
    }
}
