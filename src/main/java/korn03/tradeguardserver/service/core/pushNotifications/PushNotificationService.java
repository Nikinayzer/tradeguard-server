package korn03.tradeguardserver.service.core.pushNotifications;

import korn03.tradeguardserver.model.entity.service.PushToken;
import korn03.tradeguardserver.model.entity.service.notifications.NotificationCategory;
import korn03.tradeguardserver.model.entity.service.notifications.NotificationType;
import korn03.tradeguardserver.model.entity.service.notifications.PushNotification;
import korn03.tradeguardserver.model.repository.service.PushNotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

//TODO: DECOUPLE LATER IN PRODUCTION, THIS IS HORRIBLE
// TODO: ADD METHOD TO REMOVE TOKENS ON LOGOUT, NEED A MAPPING SERVICE BETWEEN USER-DEVICE-TOKEN
@Slf4j
@Service
public class PushNotificationService {

    private final RestTemplate restTemplate;
    private final PushNotificationRepository notificationRepository;
    private final PushTokenService pushTokenService;

    private final String EXPO_PUSH_URL= "https://exp.host/--/api/v2/push/send";

    @Autowired
    public PushNotificationService(PushNotificationRepository notificationRepository, PushTokenService pushTokenService) {
        this.restTemplate = new RestTemplate();
        this.notificationRepository = notificationRepository;
        this.pushTokenService = pushTokenService;
    }

    public void sendPushNotification(Long userId, NotificationCategory category, NotificationType type, String title, String body) {
        this.sendPushNotification(userId, category, type, title, body, Optional.<Map<String, Object>>empty());
    }

    public void sendPushNotification(Long userId, NotificationCategory category, NotificationType type, String title, String body, Optional<Map<String, Object>> dataMap) {
        List<PushToken> userTokens = pushTokenService.getPushTokensByUserId(userId);
        if (userTokens.isEmpty()) {
            log.warn("Tried to send push notification {} to user {} with no tokens", title, userId);
            return;
        }
        Map<String, Object> dataContent = dataMap.orElse(new HashMap<>());

        List<Map<String, Object>> messages = new ArrayList<>();
        for (PushToken token : userTokens) {
            Map<String, Object> message = new HashMap<>();
            message.put("to", token.getToken());
            message.put("sound", "default");
            message.put("title", title);
            message.put("body", body);
            message.put("data", dataContent);
            messages.add(message);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(messages, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(EXPO_PUSH_URL, request, String.class);
            log.info("Expo push response: {}", response.getBody());
            PushNotification notification = new PushNotification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setCategory(category);
            notification.setType(type);
            notification.setBody(body);
            notification.setData(dataContent.toString());
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);

        } catch (RestClientException ex) {
            log.error("Error sending push notification: {}", ex.getMessage());
        }
    }

    /**
     * Method to get number of unread notifications for a user
     *
     * @param userId user id to get unread notifications for
     * @return number of unread notifications
     */
    public Integer getUnreadNotificationsCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Method to mark a notification as read
     * @param userId ID of the user
     * @param notificationId ID of the notification to mark as read
     */
    public void markAsReadNotification(Long userId, Long notificationId) {
        Optional<PushNotification> notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        notification.ifPresent(pushNotification -> {
            pushNotification.setRead(true);
            notificationRepository.save(pushNotification);
        });
    }

    /**
     * Method to get all notifications for a user
     * @param userId ID of the user
     * @return List of notifications
     */
    public List<PushNotification> getNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderBySentAtDesc(userId);
    }

    public void sendMaintenanceNotification(String title, String body) {
        List<Long> userIds = pushTokenService.getAllUserIds();

        for (Long userId : userIds) {
            sendPushNotification(
                    userId,
                    NotificationCategory.SYSTEM,
                    NotificationType.INFO,
                    title,
                    body
            );
        }

        log.info("Sent maintenance notification to {} users", userIds.size());
    }
}
