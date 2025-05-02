package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.kafka.events.equity.Equity;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.service.equity.EquityService;
import korn03.tradeguardserver.endpoints.dto.user.equity.UserEquityStateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for equity data.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class EquityController {

    private final EquityService equityService;

    /**
     * Get combined equity state for a user across all venues.
     *
     * @param userId The user ID
     * @return Combined equity state
     */
    @GetMapping("/{userId}/equity")
    public ResponseEntity<UserEquityStateDTO> getUserEquityState(@PathVariable Long userId) {
        UserEquityStateDTO equityState = equityService.getUserEquityState(userId);
        if (equityState != null) {
            return ResponseEntity.ok(equityState);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get a specific equity record by user and venue.
     *
     * @param userId The user ID
     * @param venue The trading venue
     * @return The equity record if found
     */
    @GetMapping("/{userId}/equity/{venue}")
    public ResponseEntity<Equity> getEquity(@PathVariable Long userId, @PathVariable String venue) {
        Equity equity = equityService.getEquity(userId, venue);
        if (equity != null) {
            return ResponseEntity.ok(equity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 