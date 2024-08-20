package com.hackathon3.grummang_hack.controller.file;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @GetMapping
    public String hello(){
        return "Hello, files world";
    }

}
