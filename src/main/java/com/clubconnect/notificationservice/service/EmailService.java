package com.clubconnect.notificationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotification(String userEmail, String eventId, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(userEmail);
            mailMessage.setSubject("ClubConnect: New Event Notification");
            
            // Create a formatted message
            String formattedMessage = String.format(
                message.replaceAll("[{}\"]", "")  // Remove JSON formatting
                      .replaceAll(",", "\n")      // Put each field on a new line
            );
            
            mailMessage.setText(formattedMessage);

            System.out.println("Sending email to: " + userEmail);
            System.out.println("Email content:\n" + formattedMessage);

            mailSender.send(mailMessage);
            System.out.println("Email sent successfully to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}