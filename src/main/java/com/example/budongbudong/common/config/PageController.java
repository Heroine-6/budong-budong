package com.example.budongbudong.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/budongbudong")
    public String budongbudong() {
        return "forward:/index.html";
    }

    @GetMapping("/search")
    public String search() {
        return "forward:/search.html";
    }

    @GetMapping("/signin")
    public String signin() {
        return "forward:/signin.html";
    }

    @GetMapping("/signup")
    public String signup() {
        return "forward:/signup.html";
    }

    @GetMapping("/login/oauth2/code/kakao")
    public String kakaoCallback() {
        return "forward:/signin.html";
    }

    @GetMapping("/kakao-complete")
    public String kakaoComplete() {
        return "forward:/kakao-complete.html";
    }
}
