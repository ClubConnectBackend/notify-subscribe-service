package com.clubconnect.notificationservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubconnect.notificationservice.service.SubscriptionService;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Create a new subscription.
     *
     * @param userId           ID of the user.
     * @param subscriptionId   ID of the subscription.
     * @param subscriptions    Map of subscriptions (e.g., clubId, tags).
     * @return Response indicating success or failure.
     */
    @PostMapping("/{userId}/{subscriptionId}")
    public ResponseEntity<?> createSubscription(
            @PathVariable String userId,
            @PathVariable String subscriptionId,
            @RequestBody Map<String, Object> subscriptions) {
        try {
            // Validate userId and subscriptionId
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User ID cannot be null or empty.");
            }
            if (subscriptionId == null || subscriptionId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Subscription ID cannot be null or empty.");
            }

            // Validate and process the input
            Map<String, String> processedSubscriptions = new HashMap<>();
            subscriptions.forEach((key, value) -> {
                if (value instanceof String) {
                    processedSubscriptions.put(key, (String) value);
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) value;
                    processedSubscriptions.put(key, String.join(",", list)); // Store lists as comma-separated values
                }
            });

            // Save the processed subscriptions with userId and subscriptionId
            subscriptionService.addSubscription(userId, subscriptionId, processedSubscriptions);

            return ResponseEntity.status(HttpStatus.CREATED).body("Subscription created successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating subscription: " + e.getMessage());
        }
    }



   
    
        /**
     * Get a subscription by user ID and subscription ID.
     *
    * @param userId         ID of the user.
    * @param subscriptionId ID of the subscription.
    * @return Map of the subscription details or an error response.
    */
    @GetMapping("/{userId}/{subscriptionId}")
    public ResponseEntity<?> getSubscription(@PathVariable String userId, @PathVariable String subscriptionId) {
        try {
            // Fetch raw subscription data
            Map<String, AttributeValue> rawSubscription = subscriptionService.getSubscription(userId, subscriptionId);

            // Process raw AttributeValue map into a String map for user-friendly response
            Map<String, String> processedSubscription = new HashMap<>();
            if (rawSubscription != null && !rawSubscription.isEmpty()) {
                rawSubscription.forEach((key, value) -> {
                    if (value.s() != null) { // Convert only non-null string attributes
                        processedSubscription.put(key, value.s());
                    }
                });
            }

            return ResponseEntity.ok(processedSubscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving subscription: " + e.getMessage());
        }
    }



    /**
     * Delete a subscription by its ID.
     *
     * @param subscriptionId ID of the subscription.
     * @return Response indicating success or failure.
     */
    @DeleteMapping("/{userId}/{subscriptionId}")
    public ResponseEntity<?> deleteSubscription(
            @PathVariable String userId,
            @PathVariable String subscriptionId,
            @RequestBody(required = false) Map<String, String> detailsToRemove) {
        try {
            if (detailsToRemove == null || detailsToRemove.isEmpty()) {
                // If no specific details are provided, delete the entire subscription
                subscriptionService.deleteSubscription(userId, subscriptionId);
            } else {
                // Remove only specific details from the subscription
                subscriptionService.removeSubscription(userId, subscriptionId, detailsToRemove);
            }

            return ResponseEntity.ok("Subscription deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting subscription: " + e.getMessage());
        }
    }


    /**
     * Update a subscription by user ID and subscription ID.
     *
     * @param userId         ID of the user.
     * @param subscriptionId ID of the subscription.
     * @param subscriptions  Map of subscription details to update.
     * @return Response indicating success or failure.
     */
    @PutMapping("/{userId}/{subscriptionId}")
    public ResponseEntity<?> updateSubscription(
            @PathVariable String userId,
            @PathVariable String subscriptionId,
            @RequestBody Map<String, Object> subscriptions) {
        try {
            // Validate and process the request body
            Map<String, String> processedSubscriptions = new HashMap<>();
            subscriptions.forEach((key, value) -> {
                if (value instanceof String) {
                    processedSubscriptions.put(key, (String) value);
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> list = (List<String>) value;
                    processedSubscriptions.put(key, list.toString()); // Store as list representation
                }
            });

            // Call the service to update the subscription
            subscriptionService.updateSubscription(userId, subscriptionId, processedSubscriptions);

            return ResponseEntity.ok("Subscription updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating subscription: " + e.getMessage());
        }
    }


}
