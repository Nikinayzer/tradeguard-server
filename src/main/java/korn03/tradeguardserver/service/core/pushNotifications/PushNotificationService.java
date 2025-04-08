package korn03.tradeguardserver.service.core.pushNotifications;

import korn03.tradeguardserver.model.entity.service.PushNotification;
import korn03.tradeguardserver.model.entity.service.PushToken;
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

import java.util.*;

import static java.util.Optional.empty;

//TODO DECOUPLE LATER IN PRODUCTION, THIS IS HORRIBLE
@Slf4j
@Service
public class PushNotificationService {

    private final RestTemplate restTemplate;
    private final PushNotificationRepository notificationRepository;
    private final PushTokenService pushTokenService;

    @Autowired
    public PushNotificationService(PushNotificationRepository notificationRepository, PushTokenService pushTokenService) {
        this.restTemplate = new RestTemplate();
        this.notificationRepository = notificationRepository;
        this.pushTokenService = pushTokenService;
    }

    public void sendPushNotification(Long userId, String title, String body) {
        this.sendPushNotification(userId, title, body, Optional.<Map<String,Object>>empty() );
    }
    public void sendPushNotification(Long userId, String title, String body, Optional<Map<String, Object>> dataMap ) {
        List<PushToken> userTokens = pushTokenService.getPushTokensByUserId(userId);
        if (userTokens.isEmpty()) {
            log.warn("Tried to send push notification {} to user {} with no tokens",title, userId);
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

        if (!messages.isEmpty()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(messages, headers);

            try {
                String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";
                ResponseEntity<String> response = restTemplate.postForEntity(EXPO_PUSH_URL, request, String.class);
                log.info("Expo push response: {}", response.getBody());
                PushNotification notification = new PushNotification();
                notification.setUserId(userId);
                notification.setTitle(title);
                notification.setBody(body);
                notification.setData(dataContent.toString());
                notificationRepository.save(notification);

            } catch (RestClientException ex) {
                log.error("Error sending push notification: {}", ex.getMessage());
            }
        }
    }
}
