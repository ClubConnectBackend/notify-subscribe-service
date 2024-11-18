package com.clubconnect.notificationservice.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Repository
public class SubscriptionRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "Subscriptions"; // Replace with your actual table name

    @Autowired
    public SubscriptionRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    // Retrieve a subscription by subscriptionId
    public Map<String, AttributeValue> getSubscription(String userId, String subscriptionId) {
        // Validate inputs
        if (userId == null || userId.isEmpty() || subscriptionId == null || subscriptionId.isEmpty()) {
            throw new IllegalArgumentException("Both userId and subscriptionId must be provided");
        }
    
        try {
            // Build the GetItemRequest with both partition and sort keys
            GetItemRequest request = GetItemRequest.builder()
                    .tableName(tableName)
                    .key(Map.of(
                            "userId", AttributeValue.builder().s(userId).build(),
                            "subscriptionId", AttributeValue.builder().s(subscriptionId).build()
                    ))
                    .build();
    
            // Execute the request
            Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();
    
            // Debugging logs
            if (item == null || item.isEmpty()) {
                System.out.println("No subscription found for userId: " + userId + " and subscriptionId: " + subscriptionId);
            } else {
                System.out.println("Retrieved subscription: " + item);
            }
    
            return item != null ? item : new HashMap<>();
        } catch (Exception e) {
            System.err.println("Error retrieving subscription: " + e.getMessage());
            throw e;
        }
    }
    
    

    // Save a new subscription or update an existing one
    public void saveSubscription(String subscriptionId, Map<String, String> subscriptionDetails) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("subscriptionId", AttributeValue.builder().s(subscriptionId).build());

        subscriptionDetails.forEach((key, value) -> item.put(key, AttributeValue.builder().s(value).build()));

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    // Retrieve all subscriptions from the table
    public Map<String, Map<String, AttributeValue>> getAllSubscriptions() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        Map<String, Map<String, AttributeValue>> result = new HashMap<>();
        scanResponse.items().forEach(item -> {
            String subscriptionId = item.get("subscriptionId").s();
            result.put(subscriptionId, item);
        });

        return result;
    }

    // Add a new subscription or update existing subscriptions
    public void addSubscription(String userId, String subscriptionId, Map<String, String> subscriptionDetails) {
        System.out.println("Request received 1");
    
        // Validate that the subscriptionId and userId are not null or empty
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            throw new IllegalArgumentException("Subscription ID cannot be null or empty");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    
        System.out.println("Request received 2");
    
        // Create a map for the new subscription
        Map<String, AttributeValue> newSubscription = new HashMap<>();
    
        // Add the userId and subscriptionId explicitly (required for DynamoDB schema)
        newSubscription.put("userId", AttributeValue.builder().s(userId).build());
        newSubscription.put("subscriptionId", AttributeValue.builder().s(subscriptionId).build());
    
        System.out.println("Request received 3");
    
        // Add the provided subscription details
        subscriptionDetails.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                newSubscription.put(key, AttributeValue.builder().s(value).build());
            }
        });
    
        System.out.println("Request received 4");
    
        // Build the PutItemRequest
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName) // Ensure the tableName matches the actual DynamoDB table name
                .item(newSubscription)
                .build();
    
        System.out.println("Request received 5");
    
        // Save the subscription to DynamoDB
        dynamoDbClient.putItem(request);
    
        System.out.println("Subscription saved successfully: " + newSubscription);
    }
    
    
    

    // Remove specific details from a subscription
    public void removeSubscription(String userId, String subscriptionId, Map<String, String> detailsToRemove) {
        // Retrieve the existing subscription using userId and subscriptionId
        Map<String, AttributeValue> existingSubscription = getSubscription(userId, subscriptionId);
    
        if (existingSubscription == null || existingSubscription.isEmpty()) {
            throw new IllegalArgumentException("Subscription with ID " + subscriptionId + " for user " + userId + " does not exist.");
        }
    
        // Create a mutable copy of the existing subscription
        Map<String, AttributeValue> updatedSubscription = new HashMap<>(existingSubscription);
    
        // Remove only the specified details
        detailsToRemove.forEach((key, value) -> {
            if (updatedSubscription.containsKey(key)) {
                updatedSubscription.remove(key);
            }
        });
    
        // Ensure partition and sort keys are not removed
        updatedSubscription.put("userId", existingSubscription.get("userId"));
        updatedSubscription.put("subscriptionId", existingSubscription.get("subscriptionId"));
    
        // Build the PutItemRequest
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(updatedSubscription)
                .build();
    
        // Save the updated subscription back to DynamoDB
        dynamoDbClient.putItem(request);
    
        System.out.println("Subscription updated successfully for userId: " + userId + ", subscriptionId: " + subscriptionId);
    }
    
    

    // Update an existing subscription
    public void updateSubscription(String userId, String subscriptionId, Map<String, AttributeValue> updatedSubscription) {
        // Validate input
        if (userId == null || subscriptionId == null || updatedSubscription == null || updatedSubscription.isEmpty()) {
            throw new IllegalArgumentException("Invalid input for updating subscription");
        }
    
        // Ensure the keys are present
        updatedSubscription.put("userId", AttributeValue.builder().s(userId).build());
        updatedSubscription.put("subscriptionId", AttributeValue.builder().s(subscriptionId).build());
    
        // Build the PutItemRequest
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(updatedSubscription)
                .build();
    
        // Update the item in DynamoDB
        dynamoDbClient.putItem(request);
    }
    

    

    // Add this method to your SubscriptionRepository class
    public void deleteSubscription(String subscriptionId) {
        // Create the key for the subscription to delete
        Map<String, AttributeValue> key = Map.of(
            "subscriptionId", AttributeValue.builder().s(subscriptionId).build()
        );

        // Use the DynamoDB deleteItem operation to delete the subscription
        dynamoDbClient.deleteItem(builder -> builder
            .tableName(tableName)
            .key(key)
        );
    }

    public void deleteSubscription(String userId, String subscriptionId) {
        // Build the composite key for the item to delete
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("userId", AttributeValue.builder().s(userId).build());
        key.put("subscriptionId", AttributeValue.builder().s(subscriptionId).build());
    
        // Create the DeleteItemRequest
        DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                .tableName(tableName) // Replace with your DynamoDB table name
                .key(key)
                .build();
    
        // Execute the delete operation
        dynamoDbClient.deleteItem(deleteRequest);
    
        System.out.println("Subscription deleted successfully for userId: " + userId + ", subscriptionId: " + subscriptionId);
    }
    

}
