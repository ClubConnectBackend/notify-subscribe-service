package com.clubconnect.notificationservice.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clubconnect.notificationservice.config.RabbitMQConfig;

@RestController
@RequestMapping("/api/test")
public class RabbitMQTestController {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQTestController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/send")
    public String sendTestMessage(@RequestParam String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, message);
        return "Message sent to RabbitMQ: " + message;
    }
}
