package com.clubconnect.notificationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.clubconnect.notificationservice.repository.SubscriptionRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddSubscription() {
        String userId = "user1";
        String subscriptionId = "sub1";
        Map<String, String> subscriptions = Map.of("clubId", "club1", "tags", "tag1,tag2");

        subscriptionService.addSubscription(userId, subscriptionId, subscriptions);

        verify(subscriptionRepository, times(1)).addSubscription(userId, subscriptionId, subscriptions);
    }

    @Test
    void testGetSubscription() {
        String userId = "user1";
        String subscriptionId = "sub1";
        Map<String, AttributeValue> mockSubscription = Map.of(
                "clubId", AttributeValue.builder().s("club1").build(),
                "tags", AttributeValue.builder().s("tag1,tag2").build()
        );

        when(subscriptionRepository.getSubscription(userId, subscriptionId)).thenReturn(mockSubscription);

        Map<String, AttributeValue> result = subscriptionService.getSubscription(userId, subscriptionId);

        assertNotNull(result);
        assertEquals("club1", result.get("clubId").s());
        assertEquals("tag1,tag2", result.get("tags").s());
    }

    @Test
    void testUpdateSubscription() {
        String userId = "user1";
        String subscriptionId = "sub1";
        Map<String, AttributeValue> existingSubscription = Map.of(
                "clubId", AttributeValue.builder().s("club1").build(),
                "tags", AttributeValue.builder().s("tag1,tag2").build()
        );
        Map<String, String> newDetails = Map.of("tags", "tag3");

        when(subscriptionRepository.getSubscription(userId, subscriptionId)).thenReturn(existingSubscription);

        subscriptionService.updateSubscription(userId, subscriptionId, newDetails);

        verify(subscriptionRepository, times(1)).updateSubscription(eq(userId), eq(subscriptionId), any());
    }

    @Test
    void testGetSubscribedUsersForClub() {
        Map<String, Map<String, String>> allSubscriptions = Map.of(
                "sub1", Map.of("clubId", "club1", "userEmail", "user1@example.com"),
                "sub2", Map.of("clubId", "club2", "userEmail", "user2@example.com")
        );

        when(subscriptionRepository.getAllSubscriptions()).thenReturn(mockRawSubscriptions(allSubscriptions));

        Set<String> result = subscriptionService.getSubscribedUsersForClub("club1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("user1@example.com"));
    }

    @Test
    void testGetSubscribedUsersForTags() {
        Map<String, Map<String, String>> allSubscriptions = Map.of(
                "sub1", Map.of("tags", "tag1,tag2", "userEmail", "user1@example.com"),
                "sub2", Map.of("tags", "tag3", "userEmail", "user2@example.com")
        );

        when(subscriptionRepository.getAllSubscriptions()).thenReturn(mockRawSubscriptions(allSubscriptions));

        Set<String> result = subscriptionService.getSubscribedUsersForTags(new String[]{"tag1", "tag3"});

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("user1@example.com"));
        assertTrue(result.contains("user2@example.com"));
    }


    @Test
    void testDeleteSubscription() {
        String userId = "user1";
        String subscriptionId = "sub1";

        subscriptionService.deleteSubscription(userId, subscriptionId);

        verify(subscriptionRepository, times(1)).deleteSubscription(userId, subscriptionId);
    }

    private Map<String, Map<String, AttributeValue>> mockRawSubscriptions(Map<String, Map<String, String>> allSubscriptions) {
        Map<String, Map<String, AttributeValue>> rawSubscriptions = new HashMap<>();
        allSubscriptions.forEach((key, value) -> {
            Map<String, AttributeValue> attributes = new HashMap<>();
            value.forEach((k, v) -> attributes.put(k, AttributeValue.builder().s(v).build()));
            rawSubscriptions.put(key, attributes);
        });
        return rawSubscriptions;
    }
}
