package com.clubconnect.notificationservice.listner;

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

    @RabbitListener(queues = "notificationQueue")
    public void handleEventNotification(Map<String, Object> eventMessage) {
        try {
            System.out.println("Received event message: " + eventMessage);

            String eventId = (String) eventMessage.get("eventId");
            String clubId = (String) eventMessage.get("clubId");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) eventMessage.get("tags");

            if (eventId == null || clubId == null || tags == null || tags.isEmpty()) {
                System.err.println("Invalid event message: Missing or empty required fields.");
                return;
            }

            Map<String, Map<String, String>> allSubscriptions = subscriptionService.getAllSubscriptions();
            allSubscriptions.forEach((userId, userSubscriptions) -> {
                if (clubId.equals(userSubscriptions.get("clubId"))) {
                    emailService.sendNotification(userId, eventId, "New event in your subscribed club: " + clubId);
                }
                tags.forEach(tag -> {
                    if (tag.equals(userSubscriptions.get("tag"))) {
                        emailService.sendNotification(userId, eventId, "New event with your subscribed tag: " + tag);
                    }
                });
            });
        } catch (Exception e) {
            System.err.println("Error processing event notification: " + e.getMessage());
        }
    }

}
