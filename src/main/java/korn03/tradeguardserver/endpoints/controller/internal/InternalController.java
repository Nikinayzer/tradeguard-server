package korn03.tradeguardserver.endpoints.controller.internal;

import korn03.tradeguardserver.endpoints.dto.internal.UserConnectionsDTO;
import korn03.tradeguardserver.service.InternalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final InternalService internalService;

    public InternalController(InternalService internalService) {
        this.internalService = internalService;
    }

    /**
     * Get user connections data by Discord ID
     * Example: GET /internal/connections/discord/{discordId}
     */
    @GetMapping("/connections/discord/{discordId}")
    public ResponseEntity<UserConnectionsDTO> getUserConnectionsByDiscordId(@PathVariable Long discordId) {
        return ResponseEntity.ok(internalService.getUserConnectionsByDiscordId(discordId));
    }
}
