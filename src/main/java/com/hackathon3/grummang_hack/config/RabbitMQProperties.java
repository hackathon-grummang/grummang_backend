package com.hackathon3.grummang_hack.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties {
    private String exchange;
    private String groupingQueue;
    private String groupingRoutingKey;
    private String fileQueue;
    private String fileRoutingKey;
    private String vtReportQueue;
    private String vtReportRoutingKey;
    private String vtUploadQueue;
    private String vtUploadRoutingKey;
    private String GrmScanQueue;
    private String GrmScanRoutingKey;
}
