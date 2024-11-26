package com.clubconnect.notificationservice.consumer;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.clubconnect.notificationservice.config.RabbitMQConfig;
import com.clubconnect.notificationservice.service.EmailService;
import com.clubconnect.notificationservice.service.SubscriptionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class NotificationConsumer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${userEmailUrl}")
    private String userEmailUrl;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        System.out.println("Received raw message from RabbitMQ: " + message);

        try {
            // Parse the JSON string into a Map
            Map<String, Object> eventMessage = objectMapper.readValue(message, new TypeReference<>() {});
            System.out.println("Parsed event message: " + eventMessage);

            // Extract event details
            String eventId = String.valueOf(eventMessage.get("eventId"));
            String clubId = String.valueOf(eventMessage.get("clubId"));
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) eventMessage.get("tags");

            if (eventId == null || clubId == null || tags == null) {
                System.err.println("Invalid event message: Missing required fields.");
                return;
            }

            // Process subscriptions
            Map<String, Map<String, String>> allSubscriptions = subscriptionService.getAllSubscriptions();
            System.out.println("Subscriptions: " + allSubscriptions);

            allSubscriptions.forEach((subscriptionId, subscriptionDetails) -> {
                try {
                    // Extract userId from subscriptionDetails
                    String userId = subscriptionDetails.get("userId");
                    if (userId == null || userId.isEmpty()) {
                        System.err.println("No userId found for subscription ID: " + subscriptionId + ". Skipping.");
                        return;
                    }

                    System.out.println("Processing userId: " + userId);

                    // Replace {username} with userId in the userEmailUrl
                    String resolvedUserEmailUrl = userEmailUrl.replace("{username}", userId);

                    // Fetch the user email using RestTemplate
                    String userEmailAddress = restTemplate.getForObject(resolvedUserEmailUrl, String.class);

                    // Skip processing if the email is null
                    if (userEmailAddress == null || userEmailAddress.isEmpty()) {
                        System.err.println("No valid email found for userId: " + userId + ". Skipping.");
                        return;
                    }

                    System.out.println("User email for userId " + userId + ": " + userEmailAddress);

                    // Check for club subscriptions
                    if (clubId.equals(subscriptionDetails.get("clubId"))) {
                        String clubMessage = String.format(
                            "Hello!\n\n" +
                            "A new event has been added to the club you follow.\n\n" +
                            "Club ID: %s\n" +
                            "Event ID: %s\n" +
                            "Tags: %s\n\n" +
                            "Don't miss it!\n\n" +
                            "Best regards,\n" +
                            "ClubConnect Team",
                            clubId, 
                            eventId, 
                            tags.isEmpty() ? "None" : String.join(", ", tags)
                        );

                        System.out.println("Club notification email body: " + clubMessage);
                        emailService.sendNotification(userEmailAddress, eventId, clubMessage);
                    }

                    // Check for tag subscriptions
                    tags.forEach(tag -> {
                        if (tag.equals(subscriptionDetails.get("tag"))) {
                            String tagMessage = String.format(
                                "Hi!\n\n" +
                                "A new event matching your interest in '%s' has been added.\n\n" +
                                "Event ID: %s\n" +
                                "Club ID: %s\n\n" +
                                "Check it out!\n\n" +
                                "Best regards,\n" +
                                "ClubConnect Team",
                                tag, eventId, clubId
                            );

                            System.out.println("Tag notification email body: " + tagMessage);
                            emailService.sendNotification(userEmailAddress, eventId, tagMessage);
                        }
                    });
                } catch (Exception ex) {
                    System.err.println("Error processing subscription for subscription ID " + subscriptionId + " (userId: " + subscriptionDetails.get("userId") + "): " + ex.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("Error parsing or processing message: " + e.getMessage());
        }
    }
}
