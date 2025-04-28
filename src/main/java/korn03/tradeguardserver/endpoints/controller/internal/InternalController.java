package korn03.tradeguardserver.endpoints.controller.internal;

import korn03.tradeguardserver.endpoints.dto.internal.UserConnectionsDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.service.InternalService;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final InternalService internalService;
    private final UserAccountLimitsService userAccountLimitsService;

    public InternalController(InternalService internalService, UserAccountLimitsService userAccountLimitsService) {
        this.internalService = internalService;
        this.userAccountLimitsService = userAccountLimitsService;
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
}
