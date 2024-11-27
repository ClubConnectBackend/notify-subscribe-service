### README for Notification and Subscription Microservice

---

# Notification and Subscription Microservice

The **Notification and Subscription Microservice** is a core part of the **ClubConnect** platform. It handles user subscriptions to clubs or event categories and manages notifications for updates, ensuring timely and efficient dissemination of information to users. The service integrates with RabbitMQ for message queuing and AWS DynamoDB for data persistence.

---

## Features

### **Subscription Management**
- Create, retrieve, update, and delete user subscriptions.
- Associate subscriptions with specific clubs or event categories.
- Manage user preferences for targeted notifications.

### **Notification Management**
- Send email notifications to subscribed users.
- Handle real-time notification publishing using RabbitMQ.
- Efficiently disseminate updates using protocols like Gossiping.

### **Integration**
- Works seamlessly with the Club and Event microservices for end-to-end communication.

---

## Tech Stack

- **Frameworks**: Spring Boot
- **Database**: AWS DynamoDB
- **Messaging**: RabbitMQ
- **Email Service**: JavaMailSender
- **Build Tool**: Maven
- **Programming Language**: Java 17

---

## API Endpoints

### Subscription Management

| HTTP Method | Endpoint                              | Description                                           |
|-------------|---------------------------------------|-------------------------------------------------------|
| POST        | `/api/subscriptions/{userId}/{subscriptionId}` | Create a new subscription for a user.          |
| GET         | `/api/subscriptions/{userId}/{subscriptionId}` | Retrieve a specific subscription.              |
| PUT         | `/api/subscriptions/{userId}/{subscriptionId}` | Update an existing subscription.               |
| DELETE      | `/api/subscriptions/{userId}/{subscriptionId}` | Delete a subscription.                         |

---

### Notification Management

| HTTP Method | Endpoint                          | Description                                           |
|-------------|-----------------------------------|-------------------------------------------------------|
| POST        | `/api/notifications/send`         | Send a notification to subscribed users.             |

---

## Architecture

### Layers

1. **Controller Layer**:
   - Manages API endpoints for subscriptions and notifications.
   - Processes incoming requests and validates data.

2. **Service Layer**:
   - Implements business logic for managing subscriptions and notifications.
   - Integrates with repositories for data persistence.
   - Publishes notifications using RabbitMQ.

3. **Repository Layer**:
   - Handles CRUD operations for subscription data in AWS DynamoDB.

4. **Messaging Layer**:
   - Publishes and receives messages through RabbitMQ.

5. **Email Layer**:
   - Sends email notifications to users via JavaMailSender.

---

## AWS DynamoDB Schema

### Table: **Subscriptions**

| Attribute      | Type   | Description                                    |
|-----------------|--------|------------------------------------------------|
| `userId`       | String | Primary key, unique for each user.             |
| `subscriptionId` | String | Composite key, unique for each subscription.  |
| `details`      | Map    | Subscription details (e.g., clubs, tags).      |

---

### Messaging Configuration

- **Exchange Name**: `notificationExchange`
- **Queue Name**: `notificationQueue`
- **Routing Key**: `notificationKey`

---

## How to Run the Service

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repository/notification-subscription-microservice.git
   cd notification-subscription-microservice
   ```

2. **Set Up AWS Credentials**:
   Configure your AWS credentials in `~/.aws/credentials` or use environment variables:
   ```bash
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   ```

3. **Configure RabbitMQ**:
   - Add RabbitMQ credentials in `application.properties`:
     ```properties
     spring.rabbitmq.host=<RABBITMQ_HOST>
     spring.rabbitmq.username=<RABBITMQ_USERNAME>
     spring.rabbitmq.password=<RABBITMQ_PASSWORD>
     ```

4. **Build the Project**:
   ```bash
   mvn clean install
   ```

5. **Run the Service**:
   ```bash
   mvn spring-boot:run
   ```

---

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

---

## Example Requests

### Create a Subscription
**Endpoint**: `/api/subscriptions/{userId}/{subscriptionId}`  
**Method**: `POST`  
**Request Body**:
```json
{
  "clubId": "1",
  "tags": ["AI", "Technology"]
}
```

### Send a Notification
**Endpoint**: `/api/notifications/send`  
**Method**: `POST`  
**Request Body**:
```json
{
  "userEmail": "user@example.com",
  "eventId": "101",
  "message": "A new event has been added to the club you follow."
}
```

---

## Future Improvements

- Implement advanced notification scheduling and batching.
- Enhance support for multi-language email notifications.
- Integrate with SMS/Push notification services for broader reach.

