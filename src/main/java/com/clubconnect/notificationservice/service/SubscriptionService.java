package com.clubconnect.notificationservice.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.clubconnect.notificationservice.repository.SubscriptionRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Add a new subscription.
     *
     * @param subscriptionId  The ID of the subscription.
     * @param subscriptions   Map of subscription details (e.g., userId, clubId, tags).
     */
    public void addSubscription(String userId, String subscriptionId, Map<String, String> subscriptions) {
        subscriptionRepository.addSubscription(userId, subscriptionId, subscriptions);
    }

    /**
     * Get a subscription by userId and subscriptionId.
     *
     * @param userId         The ID of the user.
     * @param subscriptionId The ID of the subscription.
     * @return A map of the subscription details.
     */
    public Map<String, AttributeValue> getSubscription(String userId, String subscriptionId) {
        // Fetch the subscription from DynamoDB using the composite key
        return subscriptionRepository.getSubscription(userId, subscriptionId);
    }
    


    /**
     * Remove a subscription by its ID.
     *
     * @param subscriptionId The ID of the subscription.
     */
    public void removeSubscription(String subscriptionId) {
        subscriptionRepository.deleteSubscription(subscriptionId);
    }

    /**
     * Update a subscription by its ID.
     *
     * @param subscriptionId The ID of the subscription.
     * @param subscriptions  Map of subscription details to update.
     */
    public void updateSubscription(String userId, String subscriptionId, Map<String, String> subscriptionDetails) {
        // Retrieve existing subscription
        Map<String, AttributeValue> existingSubscription = getSubscription(userId, subscriptionId);

        if (existingSubscription == null || existingSubscription.isEmpty()) {
            throw new IllegalArgumentException("Subscription with ID " + subscriptionId + " for user " + userId + " does not exist.");
        }

        // Create a mutable map to hold updated subscription data
        Map<String, AttributeValue> updatedSubscription = new HashMap<>(existingSubscription);

        // Add or update the provided subscription details
        subscriptionDetails.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                if (key.equals("tags") && value.startsWith("[")) { // Check for list representation
                    // Parse back into a list for DynamoDB
                    List<String> tagsList = Arrays.asList(value.replace("[", "").replace("]", "").split(","));
                    updatedSubscription.put(key, AttributeValue.builder().ss(tagsList).build()); // DynamoDB list type
                } else {
                    updatedSubscription.put(key, AttributeValue.builder().s(value).build());
                }
            }
        });

        // Save the updated subscription back to DynamoDB
        subscriptionRepository.updateSubscription(userId, subscriptionId, updatedSubscription);
    }

    
    
    

    /**
     * Get a set of users subscribed to a specific club.
     *
     * @param clubId The ID of the club.
     * @return A set of user emails.
     */
    public Set<String> getSubscribedUsersForClub(String clubId) {
        Map<String, Map<String, String>> allSubscriptions = getAllSubscriptions();
        return allSubscriptions.values().stream()
                .filter(subscription -> subscription.get("clubId").equals(clubId))
                .map(subscription -> subscription.get("userEmail"))
                .collect(Collectors.toSet());
    }

    /**
     * Get a set of users subscribed to any of the given tags.
     *
     * @param tags An array of tags.
     * @return A set of user emails.
     */
    public Set<String> getSubscribedUsersForTags(String[] tags) {
        Map<String, Map<String, String>> allSubscriptions = getAllSubscriptions();
        Set<String> tagsSet = new HashSet<>(Arrays.asList(tags));

        return allSubscriptions.values().stream()
                .filter(subscription -> {
                    String subscriptionTags = subscription.get("tags");
                    if (subscriptionTags == null || subscriptionTags.isEmpty()) {
                        return false;
                    }
                    Set<String> userTags = new HashSet<>(Arrays.asList(subscriptionTags.split(",")));
                    return !Collections.disjoint(tagsSet, userTags);
                })
                .map(subscription -> subscription.get("userEmail"))
                .collect(Collectors.toSet());
    }

    /**
     * Merge the subscribers for clubs and tags into a single set to avoid duplicates.
     *
     * @param clubSubscribers A set of user emails subscribed to clubs.
     * @param tagSubscribers  A set of user emails subscribed to tags.
     * @return A combined set of unique user emails.
     */
    public Set<String> mergeSubscribers(Set<String> clubSubscribers, Set<String> tagSubscribers) {
        Set<String> allSubscribers = new HashSet<>(clubSubscribers);
        allSubscribers.addAll(tagSubscribers);
        return allSubscribers;
    }

    /**
     * Retrieve all subscriptions from the repository.
     *
     * @return A map of all subscriptions with subscription IDs as keys.
     */
    public Map<String, Map<String, String>> getAllSubscriptions() {
        Map<String, Map<String, AttributeValue>> rawSubscriptions = subscriptionRepository.getAllSubscriptions();

        Map<String, Map<String, String>> processedSubscriptions = new HashMap<>();
        rawSubscriptions.forEach((subscriptionId, subscriptionDetails) -> {
            Map<String, String> processedDetails = new HashMap<>();
            subscriptionDetails.forEach((key, value) -> {
                if (value.s() != null) {
                    processedDetails.put(key, value.s());
                }
            });
            processedSubscriptions.put(subscriptionId, processedDetails);
        });

        return processedSubscriptions;
    }


    public void removeSubscription(String userId, String subscriptionId, Map<String, String> detailsToRemove) {
        Map<String, AttributeValue> existingSubscription = getSubscription(userId, subscriptionId);
    
        if (existingSubscription == null || existingSubscription.isEmpty()) {
            throw new IllegalArgumentException("Subscription with ID " + subscriptionId + " for user " + userId + " does not exist.");
        }
    
        detailsToRemove.forEach((key, value) -> existingSubscription.remove(key));
    
        subscriptionRepository.updateSubscription(userId, subscriptionId, existingSubscription);
    }

    public void deleteSubscription(String userId, String subscriptionId) {
        subscriptionRepository.deleteSubscription(userId, subscriptionId);
    }
    
    
}
