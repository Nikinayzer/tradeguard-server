package korn03.tradeguardserver.endpoints.controller.user.notifications;

import korn03.tradeguardserver.model.entity.service.notifications.PushNotification;
import korn03.tradeguardserver.service.core.pushNotifications.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/users/")
public class NotificationsController {

    private final PushNotificationService pushNotificationService;

    public NotificationsController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    /**
     * Get all notifications for a user.
     *
     * @param userId The user ID
     * @return List of notifications
     */
    @GetMapping("{userId}/notifications")
    public ResponseEntity<List<PushNotification>> getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(pushNotificationService.getNotifications(userId));
    }

    /**
     * Get unread notifications count for a user.
     *
     * @param userId The user ID
     * @return Integer of unread notifications
     */
    @GetMapping("{userId}/notifications/unread")
    public ResponseEntity<Integer> getUnreadNotificationsCount(@PathVariable Long userId) {
        return ResponseEntity.ok(pushNotificationService.getUnreadNotificationsCount(userId));
    }

    /**
     * Mark a notification as read.
     *
     * @param userId         The user ID
     * @param notificationId The notification ID
     * @return void
     */
    @PostMapping("{userId}/notifications/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long userId, @PathVariable Long notificationId) {
        pushNotificationService.markAsReadNotification(userId, notificationId);
        return ResponseEntity.ok().build();
    }
}
