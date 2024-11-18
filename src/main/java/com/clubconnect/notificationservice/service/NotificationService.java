// package com.clubconnect.notificationservice.service;

// import java.util.Map;
// import java.util.Set;

// import org.springframework.amqp.rabbit.annotation.RabbitListener;
// import org.springframework.stereotype.Service;

// @Service
// public class NotificationService {

//     private final SubscriptionService subscriptionService;
//     private final EmailService emailService;

//     public NotificationService(SubscriptionService subscriptionService, EmailService emailService) {
//         this.subscriptionService = subscriptionService;
//         this.emailService = emailService;
//     }

//     /**
//      * Listens to messages from RabbitMQ and processes event notifications.
//      *
//      * @param message A map containing event details.
//      *                Example: {"eventId": "101", "tags": "AI,ML", "clubId": "5"}
//      */
//     @RabbitListener(queues = "notificationQueue") // Replace with your queue name
//     public void handleEventNotification(Map<String, String> message) {
//         try {
//             String eventId = message.get("eventId");
//             String clubId = message.get("clubId");
//             String tags = message.get("tags"); // Comma-separated tags

//             System.out.println("Received message from RabbitMQ: " + message);

//             // Get users subscribed to the club
//             Set<String> clubSubscribers = subscriptionService.getSubscribedUsersForClub(clubId);

//             // Get users subscribed to the tags
//             Set<String> tagSubscribers = subscriptionService.getSubscribedUsersForTags(tags.split(","));

//             // Merge and notify unique users
//             Set<String> allSubscribers = subscriptionService.mergeSubscribers(clubSubscribers, tagSubscribers);

//             for (String userEmail : allSubscribers) {
//                 emailService.sendNotification(userEmail, eventId, "A new event has been created with the following details: " + message);
//             }
//         } catch (Exception e) {
//             System.err.println("Error processing event notification: " + e.getMessage());
//         }
//     }
// }
