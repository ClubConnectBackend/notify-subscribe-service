package com.clubconnect.notificationservice.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.clubconnect.notificationservice.config.RabbitMQConfig;

@Component
public class NotificationConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println("Received message from RabbitMQ: " + message);

        // Send email notification
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo("chhedavishwa21@gmail.com"); // Replace with recipient email
            mailMessage.setSubject("Notification");
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}
