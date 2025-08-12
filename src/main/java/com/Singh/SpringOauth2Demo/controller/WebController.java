package com.Singh.SpringOauth2Demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @GetMapping("/")
    public String index() {
        return "upload.html";
    }
    
    @GetMapping("/upload")
    public String upload() {
        return "upload.html";
    }
}