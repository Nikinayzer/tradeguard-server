package korn03.tradeguardserver.endpoints.controller.internal;

import korn03.tradeguardserver.endpoints.dto.internal.UserConnectionsDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.service.InternalService;
import korn03.tradeguardserver.service.core.pushNotifications.PushNotificationService;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final InternalService internalService;
    private final UserAccountLimitsService userAccountLimitsService;
    private final PushNotificationService pushNotificationService;

    public InternalController(InternalService internalService, UserAccountLimitsService userAccountLimitsService, PushNotificationService pushNotificationService) {
        this.internalService = internalService;
        this.userAccountLimitsService = userAccountLimitsService;
        this.pushNotificationService = pushNotificationService;
    }

    /**
     * Get user connections data by Discord ID
     * Example: GET /internal/connections/discord/{discordId}
     */
    @GetMapping("/connections/discord/{discordId}")
    public ResponseEntity<UserConnectionsDTO> getUserConnectionsByDiscordId(@PathVariable Long discordId) {
        return ResponseEntity.ok(internalService.getUserConnectionsByDiscordId(discordId));
    }

    @GetMapping("/users/{userId}/limits")
    public ResponseEntity<UserAccountLimitsDTO> getUserLimits(@PathVariable Long userId) {
        return ResponseEntity.ok(userAccountLimitsService.getLimitsByUserId(userId));
    }

    /**
     * Sends a maintenance notification to all users.
     * @param body JSON body containing the title and message of the notification.
     * @return ResponseEntity with status OK.
     */
    @PostMapping("/notifications/send/maintenance")
    public ResponseEntity<Void> sendMaintenanceNotification(
            @RequestBody Map<String, String> body
    ) {
        pushNotificationService.sendMaintenanceNotification(body.get("title"), body.get("message"));
        return ResponseEntity.ok().build();
    }
}
