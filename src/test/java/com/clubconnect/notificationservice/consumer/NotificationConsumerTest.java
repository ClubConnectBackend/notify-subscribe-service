package com.clubconnect.notificationservice.consumer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.clubconnect.notificationservice.service.EmailService;
import com.clubconnect.notificationservice.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;

class NotificationConsumerTest {

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Mock
    private EmailService emailService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper;

    @Value("${userEmailUrl}")
    private String userEmailUrl = "https://example.com/api/auth/email/{username}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReceiveMessage_InvalidMessage() throws Exception {
        // Mock invalid message (missing eventId)
        String invalidMessage = "{\"tags\":[\"Technology\"],\"clubId\":\"123\"}";

        // Mock the subscription service behavior to avoid unintended interaction errors
        when(subscriptionService.getAllSubscriptions()).thenReturn(Map.of());

        // Execute the method
        notificationConsumer.receiveMessage(invalidMessage);

        // Verify no email is sent and subscription service is called
        verify(emailService, never()).sendNotification(anyString(), anyString(), anyString());
        verify(subscriptionService, times(1)).getAllSubscriptions(); // Ensure it was called once
    }


    @Test
    void testReceiveMessage_NoMatchingSubscription() throws Exception {
        // Mock input message
        String message = "{\"eventId\":\"101\",\"tags\":[\"Technology\"],\"clubId\":\"123\"}";
        Map<String, Map<String, String>> mockSubscriptions = Map.of(
                "user2", Map.of("clubId", "456", "tag", "Other")
        );

        // Mock external interactions
        when(subscriptionService.getAllSubscriptions()).thenReturn(mockSubscriptions);

        // Execute the method
        notificationConsumer.receiveMessage(message);

        // Verify no email is sent
        verifyNoInteractions(emailService);
    }

    @Test
    void testReceiveMessage_EmailNotFound() throws Exception {
        // Mock input message
        String message = "{\"eventId\":\"101\",\"tags\":[\"Technology\"],\"clubId\":\"123\"}";
        Map<String, Map<String, String>> mockSubscriptions = Map.of(
                "user1", Map.of("clubId", "123", "tag", "Technology")
        );

        // Mock external interactions
        when(subscriptionService.getAllSubscriptions()).thenReturn(mockSubscriptions);
        when(restTemplate.getForObject("https://example.com/api/auth/email/user1", String.class))
                .thenReturn(null); // No email found

        // Execute the method
        notificationConsumer.receiveMessage(message);

        // Verify no email is sent
        verify(emailService, never()).sendNotification(anyString(), anyString(), anyString());
    }
}
