package korn03.tradeguardserver.endpoints.dto.user.position;

import korn03.tradeguardserver.kafka.events.position.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing a user's positions state across all venues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPositionsStateDTO {
    
    /**
     * The user ID
     */
    private Long userId;
    
    /**
     * Total position value across all venues (in USDT)
     */
    private BigDecimal totalPositionValue;
    
    /**
     * Total unrealized PnL across all positions (in USDT)
     */
    private BigDecimal totalUnrealizedPnl;
    
    /**
     * The timestamp of this positions state snapshot
     */
    private Instant timestamp;
    
    /**
     * Active positions (with non-zero size)
     */
    private List<Position> activePositions;
    
    /**
     * Inactive positions (with zero size)
     */
    private List<Position> inactivePositions;
    
    /**
     * Total count of positions (active + inactive)
     */
    private int totalPositionsCount;
    
    /**
     * Count of active positions
     */
    private int activePositionsCount;
} 