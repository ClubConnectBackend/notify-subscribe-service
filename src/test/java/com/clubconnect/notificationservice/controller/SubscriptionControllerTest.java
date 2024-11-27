package com.clubconnect.notificationservice.controller;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.clubconnect.notificationservice.service.SubscriptionService;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class SubscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController).build();
    }

    @Test
    void testCreateSubscription_Success() throws Exception {
        String userId = "user123";
        String subscriptionId = "sub456";
        String requestBody = "{\"clubId\":\"123\", \"tags\":[\"Technology\", \"AI\"]}";

        doNothing().when(subscriptionService).addSubscription(eq(userId), eq(subscriptionId), anyMap());

        mockMvc.perform(post("/api/subscriptions/{userId}/{subscriptionId}", userId, subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string("Subscription created successfully."));

        verify(subscriptionService, times(1)).addSubscription(eq(userId), eq(subscriptionId), anyMap());
    }

    @Test
    void testGetSubscription_Success() throws Exception {
        String userId = "user123";
        String subscriptionId = "sub456";
        Map<String, AttributeValue> mockSubscription = Map.of(
                "clubId", AttributeValue.builder().s("123").build(),
                "tags", AttributeValue.builder().s("Technology,AI").build()
        );

        when(subscriptionService.getSubscription(userId, subscriptionId)).thenReturn(mockSubscription);

        mockMvc.perform(get("/api/subscriptions/{userId}/{subscriptionId}", userId, subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value("123"))
                .andExpect(jsonPath("$.tags").value("Technology,AI"));

        verify(subscriptionService, times(1)).getSubscription(userId, subscriptionId);
    }

    @Test
    void testUpdateSubscription_Success() throws Exception {
        String userId = "user123";
        String subscriptionId = "sub456";
        String requestBody = "{\"tags\":\"Technology,AI\"}";

        doNothing().when(subscriptionService).updateSubscription(eq(userId), eq(subscriptionId), anyMap());

        mockMvc.perform(put("/api/subscriptions/{userId}/{subscriptionId}", userId, subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription updated successfully."));

        verify(subscriptionService, times(1)).updateSubscription(eq(userId), eq(subscriptionId), anyMap());
    }

    @Test
    void testDeleteSubscription_Success() throws Exception {
        String userId = "user123";
        String subscriptionId = "sub456";

        doNothing().when(subscriptionService).deleteSubscription(userId, subscriptionId);

        mockMvc.perform(delete("/api/subscriptions/{userId}/{subscriptionId}", userId, subscriptionId))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription deleted successfully."));

        verify(subscriptionService, times(1)).deleteSubscription(userId, subscriptionId);
    }

    @Test
    void testDeleteSubscription_SpecificDetails() throws Exception {
        String userId = "user123";
        String subscriptionId = "sub456";
        String requestBody = "{\"clubId\":\"123\"}";

        doNothing().when(subscriptionService).removeSubscription(eq(userId), eq(subscriptionId), anyMap());

        mockMvc.perform(delete("/api/subscriptions/{userId}/{subscriptionId}", userId, subscriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscription deleted successfully."));

        verify(subscriptionService, times(1)).removeSubscription(eq(userId), eq(subscriptionId), anyMap());
    }
}
