package com.hackathon3.grummang_hack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BaseConfig {


    //restTemplate
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
