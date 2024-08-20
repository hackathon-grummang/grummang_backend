package com.hackathon3.grummang_hack.config;

import org.springframework.web.client.RestTemplate;

public class BaseConfig {


    //restTemplate
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
