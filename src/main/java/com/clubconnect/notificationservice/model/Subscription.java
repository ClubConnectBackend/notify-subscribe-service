package com.clubconnect.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {
    private String subscriptionId; // Unique ID for the subscription
    private String userId;         // User ID subscribing
    private String type;           // 'club' or 'tag'
    private String clubId;         // Club ID if type = 'club'
    private String tag;            // Tag if type = 'tag'
    private String preferences;    // Email or Push Notifications
}
