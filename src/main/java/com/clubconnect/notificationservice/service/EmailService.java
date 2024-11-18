package com.clubconnect.notificationservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private String fromEmail = "bhavindewani@gmail.com"; // Sender's email address from application properties

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a notification email to the user.
     *
     * @param userEmail The recipient's email address
     * @param eventId   The ID of the event being notified
     * @param message   The notification message
     */
    public void sendNotification(String userEmail, String eventId, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo("chhedavishwa21@gmail.com");
            mailMessage.setSubject("ClubConnect: Event Notification");
            mailMessage.setText(String.format("Event ID: %s%n%s", eventId, message));

            mailSender.send(mailMessage);
            System.out.println("Notification email sent to: " + userEmail);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }
}
