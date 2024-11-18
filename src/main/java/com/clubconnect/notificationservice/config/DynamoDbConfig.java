package com.clubconnect.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Configuration class to set up the DynamoDB client.
 */
@Configuration
public class DynamoDbConfig {

    /**
     * Creates a DynamoDB client bean to interact with DynamoDB in the specified region.
     *
     * @return DynamoDbClient instance
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1)  // Specify your AWS region
                .build();
    }
}
