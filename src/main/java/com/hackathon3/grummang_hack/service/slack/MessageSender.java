package com.hackathon3.grummang_hack.service.slack;


import com.hackathon3.grummang_hack.config.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageSender {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitTemplate groupingRabbitTemplate;
    private final RabbitMQProperties properties;

    @Autowired
    public MessageSender(@Qualifier("rabbitTemplate") RabbitTemplate rabbitTemplate,
                         @Qualifier("groupingRabbitTemplate") RabbitTemplate groupingRabbitTemplate,
                         RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.groupingRabbitTemplate = groupingRabbitTemplate;
        this.properties = properties;
    }

    public void sendMessage(Long message) {
        rabbitTemplate.convertAndSend(properties.getFileRoutingKey(),message);
        System.out.println("Sent message to default queue: " + message);
    }

    public void sendGroupingMessage(Long message) {
        groupingRabbitTemplate.convertAndSend(message);
        System.out.println("Sent message to grouping queue: " + message);
    }

    public void sendMessageToQueue(Long message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("Sent message to queue " + queueName + ": " + message);
    }
}
