package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.endpoints.dto.user.position.UserPositionsStateDTO;
import korn03.tradeguardserver.kafka.events.position.Position;
import korn03.tradeguardserver.service.position.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for position data.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /**
     * Get the complete positions state for a user including both active and inactive positions.
     *
     * @param userId The user ID
     * @return Complete positions state
     */
    @GetMapping("/{userId}/positions/state")
    public ResponseEntity<UserPositionsStateDTO> getUserPositionsState(@PathVariable Long userId) {
        UserPositionsStateDTO positionsState = positionService.getUserPositionsState(userId);
        if (positionsState != null) {
            return ResponseEntity.ok(positionsState);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Get a specific position by venue and symbol.
     *
     * @param userId The user ID
     * @param venue The trading venue
     * @param symbol The trading symbol
     * @return The position if found
     */
    @GetMapping("/{userId}/positions/{venue}-{symbol}")
    public ResponseEntity<Position> getPosition(
            @PathVariable Long userId,
            @PathVariable String venue, 
            @PathVariable String symbol) {
        Position position = positionService.getPosition(userId, venue, symbol);
        if (position != null) {
            return ResponseEntity.ok(position);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 