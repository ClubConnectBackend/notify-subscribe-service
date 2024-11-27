package com.clubconnect.notificationservice.producer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.clubconnect.notificationservice.config.RabbitMQConfig;

class NotificationProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private NotificationProducer notificationProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendNotification_Success() {
        String message = "Test notification message";

        // Call the method
        notificationProducer.sendNotification(message);

        // Verify that RabbitTemplate's specific convertAndSend method was called
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME), 
                eq(RabbitMQConfig.ROUTING_KEY), 
                eq(message)
        );

        System.out.println("Test passed: Message sent successfully.");
    }

    @Test
    void testSendNotification_NullMessage() {
        String message = null;

        // Call the method
        notificationProducer.sendNotification(message);

        // Verify that RabbitTemplate's specific convertAndSend method was called with null
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY),
                eq(message)
        );

        System.out.println("Test passed: Null message handled gracefully.");
    }
}
