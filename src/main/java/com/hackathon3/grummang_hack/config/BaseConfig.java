package com.hackathon3.grummang_hack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BaseConfig {


    //restTemplate
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
