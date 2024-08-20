package com.hackathon3.grummang_hack.service.google;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageReceiver {

    private final DriveInitService driveInitService;

    @Autowired
    public MessageReceiver(DriveInitService driveInitService) {
        this.driveInitService = driveInitService;
    }
    @RabbitListener(queues = "${rabbitmq.GOOGLE_INIT_QUEUE}")
    public void receiveMessage(int message) {
        try {
            log.info("Received message from queue: " + message);
            driveInitService.fetchAndSaveAll(message);
        } catch (Exception e) {
            log.error("An error occurred while processing the message: {}", e.getMessage(), e);
        }
    }
}
