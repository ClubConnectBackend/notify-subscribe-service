package com.clubconnect.notificationservice.listener;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.clubconnect.notificationservice.service.EmailService;
import com.clubconnect.notificationservice.service.SubscriptionService;

@Service
public class EventListener {

    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    public EventListener(SubscriptionService subscriptionService, EmailService emailService) {
        this.subscriptionService = subscriptionService;
        this.emailService = emailService;
    }

    @RabbitListener(queues = "notificationQueue") // Queue name for RabbitMQ
    public void handleEventNotification(Map<String, Object> eventMessage) {
        try {
            System.out.println("Received event message: " + eventMessage);

            // Extract and validate event details
            String eventId = (String) eventMessage.get("eventId");
            String clubId = (String) eventMessage.get("clubId");
            List<String> tags = (List<String>) eventMessage.get("tags"); // Adjusted to use List<String>

            if (eventId == null || clubId == null || tags == null) {
                System.err.println("Invalid event message: Missing required fields.");
                return;
            }

            // Get all subscriptions
            Map<String, Map<String, String>> allSubscriptions = subscriptionService.getAllSubscriptions();

            for (Map.Entry<String, Map<String, String>> entry : allSubscriptions.entrySet()) {
                String userId = entry.getKey();
                Map<String, String> userSubscriptions = entry.getValue();

                // Notify users subscribed to the event's club
                if (clubId.equals(userSubscriptions.get("clubId"))) {
                    emailService.sendNotification(userId, eventId, "New event in your subscribed club!");
                }

                // Notify users subscribed to the event's tags
                for (String tag : tags) {
                    if (tag.equals(userSubscriptions.get("tag"))) {
                        emailService.sendNotification(userId, eventId, "New event with your subscribed tag!");
                    }
                }
            }
        } catch (ClassCastException e) {
            System.err.println("Error processing event message: Invalid data types in message payload.");
        } catch (Exception e) {
            System.err.println("Error processing event notification: " + e.getMessage());
        }
    }
}
