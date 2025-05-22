package korn03.tradeguardserver.service.position;

import korn03.tradeguardserver.kafka.events.position.PositionKafkaDTO;
import korn03.tradeguardserver.endpoints.dto.user.position.UserPositionsStateDTO;
import korn03.tradeguardserver.mapper.PositionMapper;
import korn03.tradeguardserver.service.core.CacheService;
import korn03.tradeguardserver.service.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Set;

/**
 * Service for managing position data from Kafka in Redis cache.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PositionService {

    private final CacheService cacheService;
    private final SseEmitterService sseService;
    private final PositionMapper positionMapper;
    private static final Duration POSITION_TTL = Duration.ofMinutes(5);
    private static final String POSITION_KEY_PREFIX = "position:";
    
    // In-memory map to track known position keys
    private final Map<String, Boolean> knownPositionKeys = new ConcurrentHashMap<>();

    /**
     * Process and store a position update received from Kafka.
     * 
     * @param position The position data to store
     */
    public void processPositionUpdate(PositionKafkaDTO position) {
        if (position == null || position.getPosition() == null) {
            log.warn("Received null position update or position details");
            return;
        }
        
        try {
            String key = POSITION_KEY_PREFIX + position.getUserId() + ":" + position.getPositionKey();
            cacheService.storeInCache(key, position, POSITION_TTL);
            knownPositionKeys.put(key, true);
            log.debug("Stored position update for key: {}", key);
            
            // Send SSE update with full positions state
            UserPositionsStateDTO positionsState = getUserPositionsState(position.getUserId());
            if (positionsState != null) {
                log.info("Sending position update via SSE for user {}", position.getUserId());
                sseService.sendUpdate(position.getUserId(), "positions", positionsState);
            } else {
                log.warn("Could not generate positions state for user {}", position.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing position update for {}: {}", position.getPositionKey(), e.getMessage(), e);
        }
    }
    
    /**
     * Get a specific position by its key.
     * 
     * @param userId The user ID
     * @param venue The trading venue
     * @param symbol The trading symbol
     * @return The position if found, null otherwise
     */
    public PositionKafkaDTO getPosition(Long userId, String venue, String symbol) {
        String positionKey = venue + "_" + symbol;
        String cacheKey = POSITION_KEY_PREFIX + userId + ":" + positionKey;
        return cacheService.getFromCache(cacheKey, PositionKafkaDTO.class);
    }
    
    /**
     * Get all positions for a specific user.
     * 
     * @param userId The user ID
     * @return List of positions for the user
     */
    public List<PositionKafkaDTO> getUserPositions(Long userId) {
        String userKeyPrefix = POSITION_KEY_PREFIX + userId + ":";
        List<PositionKafkaDTO> userPositions = new ArrayList<>();
        
        for (String key : knownPositionKeys.keySet()) {
            if (key.startsWith(userKeyPrefix)) {
                PositionKafkaDTO position = cacheService.getFromCache(key, PositionKafkaDTO.class);
                if (position != null) {
                    userPositions.add(position);
                    log.debug("Retrieved position from cache: {}", key);
                } else {
                    log.warn("Position not found in cache for key: {}", key);
                }
            }
        }
        
        log.info("Found {} positions for user {}", userPositions.size(), userId);
        return userPositions;
    }
    
    /**
     * Get all active positions (non-zero size) for a user across all venues.
     * Organizes positions by venue and calculates aggregate totals.
     * 
     * @param userId The user ID
     * @return A DTO containing active positions and aggregated data
     */
    public UserPositionsStateDTO getUserActivePositions(Long userId) {
        List<PositionKafkaDTO> allUserPositions = getUserPositions(userId);
        if (allUserPositions.isEmpty()) {
            return null;
        }
        
        List<PositionKafkaDTO> activePositions = filterActivePositions(allUserPositions);
        if (activePositions.isEmpty()) {
            return null;
        }
        
        PositionTotals totals = calculatePositionTotals(activePositions);
        Instant latestTimestamp = findLatestTimestamp(activePositions);
        
        return UserPositionsStateDTO.builder()
                .summary(UserPositionsStateDTO.PositionSummary.builder()
                    .totalPositionValue(totals.totalPositionValue)
                    .totalUnrealizedPnl(totals.totalUnrealizedPnl)
                    .totalPositionsCount(allUserPositions.size())
                    .activePositionsCount(activePositions.size())
                    .lastUpdate(latestTimestamp)
                    .build())
                .activePositions(positionMapper.toFrontendDTOList(activePositions))
                .build();
    }
    
    /**
     * Get the complete positions state for a user including both active and inactive positions.
     * 
     * @param userId The user ID
     * @return A DTO containing complete position state information
     */
    public UserPositionsStateDTO getUserPositionsState(Long userId) {
        List<PositionKafkaDTO> allUserPositions = getUserPositions(userId);
        if (allUserPositions.isEmpty()) {
            return null;
        }
        
        List<PositionKafkaDTO> activePositions = filterActivePositions(allUserPositions);
        List<PositionKafkaDTO> inactivePositions = filterInactivePositions(allUserPositions);
        
        PositionTotals totals = calculatePositionTotals(activePositions);
        Instant latestTimestamp = findLatestTimestamp(allUserPositions);
        
        return UserPositionsStateDTO.builder()
                .summary(UserPositionsStateDTO.PositionSummary.builder()
                    .totalPositionValue(totals.totalPositionValue)
                    .totalUnrealizedPnl(totals.totalUnrealizedPnl)
                    .totalPositionsCount(allUserPositions.size())
                    .activePositionsCount(activePositions.size())
                    .lastUpdate(latestTimestamp)
                    .build())
                .activePositions(positionMapper.toFrontendDTOList(activePositions))
                .inactivePositions(positionMapper.toFrontendDTOList(inactivePositions))
                .build();
    }
    
    /**
     * Get all currently stored positions.
     * 
     * @return List of positions
     */
    public List<PositionKafkaDTO> getAllPositions() {
        return knownPositionKeys.keySet().stream()
                .map(key -> cacheService.getFromCache(key, PositionKafkaDTO.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get the known position cache keys.
     * 
     * @return Set of known position keys
     */
    public Set<String> getKnownPositionKeys() {
        return knownPositionKeys.keySet();
    }

    private List<PositionKafkaDTO> filterActivePositions(List<PositionKafkaDTO> positions) {
        return positions.stream()
                .filter(position -> position.getPosition() != null && 
                        position.getPosition().getQty() != null && 
                        position.getPosition().getQty().compareTo(BigDecimal.ZERO) != 0)
                .collect(Collectors.toList());
    }

    private List<PositionKafkaDTO> filterInactivePositions(List<PositionKafkaDTO> positions) {
        return positions.stream()
                .filter(position -> position.getPosition() == null || 
                        position.getPosition().getQty() == null || 
                        position.getPosition().getQty().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList());
    }

    private PositionTotals calculatePositionTotals(List<PositionKafkaDTO> positions) {
        BigDecimal totalPositionValue = BigDecimal.ZERO;
        BigDecimal totalUnrealizedPnl = BigDecimal.ZERO;
        
        for (PositionKafkaDTO position : positions) {
            PositionKafkaDTO.PositionDetailsKafkaDTO details = position.getPosition();
            if (details.getUsdtAmt() != null) {
                totalPositionValue = totalPositionValue.add(details.getUsdtAmt());
            }
            if (details.getUnrealizedPnl() != null) {
                totalUnrealizedPnl = totalUnrealizedPnl.add(details.getUnrealizedPnl());
            }
        }
        
        return new PositionTotals(totalPositionValue, totalUnrealizedPnl);
    }

    private Instant findLatestTimestamp(List<PositionKafkaDTO> positions) {
        return positions.stream()
                .map(position -> Instant.parse(position.getTimestamp()))
                .max(Instant::compareTo)
                .orElse(Instant.now());
    }

    @lombok.Value
    private static class PositionTotals {
        BigDecimal totalPositionValue;
        BigDecimal totalUnrealizedPnl;
    }
} 