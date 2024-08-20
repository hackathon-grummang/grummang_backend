package com.hackathon3.grummang_hack.service.file;

import com.hackathon3.grummang_hack.config.RabbitMQProperties;
import com.hackathon3.grummang_hack.service.vt.FileStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class FileEventListenerService {
    private static final Logger logger = LoggerFactory.getLogger(FileEventListenerService.class);

    private final RabbitTemplate rabbitTemplate;
    private final FileStatusService fileStatusService;
    private final RabbitMQProperties properties;

    public FileEventListenerService(RabbitTemplate rabbitTemplate, FileStatusService fileStatusService, RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.fileStatusService = fileStatusService;
        this.properties = properties;
    }

    @RabbitListener(queues = "#{@rabbitMQProperties.fileQueue}")
    public void executeFileTask(long fileId) {
        try {
            logger.info("Processing file with ID: {}", fileId);

            fileStatusService.createFileStatus(fileId);

            rabbitTemplate.convertAndSend(properties.getExchange(), properties.getVtReportRoutingKey(), fileId);

        } catch (Exception e) {
            logger.error("Error processing file with ID: " + fileId, e);
        }
    }
}
