package korn03.tradeguardserver.service.equity;

import korn03.tradeguardserver.kafka.events.equity.Equity;
import korn03.tradeguardserver.service.core.CacheService;
import korn03.tradeguardserver.service.event.SseEmitterService;
import korn03.tradeguardserver.endpoints.dto.user.equity.UserEquityStateDTO;
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
 * Service for managing equity data from Kafka in Redis cache.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EquityService {

    private final CacheService cacheService;
    private final SseEmitterService sseService;
    private static final Duration EQUITY_TTL = Duration.ofMinutes(1);
    private static final String EQUITY_KEY_PREFIX = "equity:";
    
    // In-memory map to track known equity keys
    private final Map<String, Boolean> knownEquityKeys = new ConcurrentHashMap<>();

    /**
     * Process and store an equity update received from Kafka.
     * 
     * @param equity The equity data to store
     */
    public void processEquityUpdate(Equity equity) {
        if (equity == null) {
            log.warn("Received null equity update");
            return;
        }
        
        try {
            String key = EQUITY_KEY_PREFIX + equity.getUserId()+ ":" + equity.getEquityKey();
            cacheService.storeInCache(key, equity, EQUITY_TTL);
            knownEquityKeys.put(key, true);
            log.debug("Stored equity update for key: {}", key);
            
            // Send SSE update with full equity state
            UserEquityStateDTO equityState = getUserEquityState(equity.getUserId());
            if (equityState != null) {
                log.info("Sending equity update via SSE for user {}", equity.getUserId());
                sseService.sendUpdate(equity.getUserId(), "equity", equityState);
            } else {
                log.warn("Could not generate equity state for user {}", equity.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing equity update for {}: {}", equity.getEquityKey(), e.getMessage(), e);
        }
    }
    
    /**
     * Get a specific equity record by user and venue.
     * 
     * @param userId The user ID
     * @param venue The trading venue
     * @return The equity record if found, null otherwise
     */
    public Equity getEquity(Long userId, String venue) {
        String cacheKey = EQUITY_KEY_PREFIX + userId + ":" + venue;
        return cacheService.getFromCache(cacheKey, Equity.class);
    }
    
    /**
     * Get all equity records for a specific user.
     * 
     * @param userId The user ID
     * @return List of equity records for the user
     */
    public List<Equity> getUserEquities(Long userId) {
        String userKeyPrefix = EQUITY_KEY_PREFIX + userId + ":";
        List<Equity> userEquities = new ArrayList<>();
        
        for (String key : knownEquityKeys.keySet()) {
            if (key.startsWith(userKeyPrefix)) {
                Equity equity = cacheService.getFromCache(key, Equity.class);
                if (equity != null) {
                    userEquities.add(equity);
                    log.debug("Retrieved equity from cache: {}", key);
                } else {
                    log.warn("Equity not found in cache for key: {}", key);
                }
            }
        }
        
        log.info("Found {} equity records for user {}", userEquities.size(), userId);
        return userEquities;
    }
    
    /**
     * Get a consolidated view of a user's equity across all venues.
     * 
     * @param userId The user ID
     * @return Combined equity state DTO or null if no equity records exist
     */
    public UserEquityStateDTO getUserEquityState(Long userId) {
        List<Equity> userEquities = getUserEquities(userId);
        
        if (userEquities.isEmpty()) {
            return null;
        }
        
        BigDecimal totalWalletBalance = BigDecimal.ZERO;
        BigDecimal totalAvailableBalance = BigDecimal.ZERO;
        BigDecimal totalUnrealizedPnl = BigDecimal.ZERO;
        BigDecimal totalBnbBalanceUsdt = BigDecimal.ZERO;
        for (Equity equity : userEquities) {
            totalWalletBalance = totalWalletBalance.add(equity.getWalletBalance());
            totalAvailableBalance = totalAvailableBalance.add(equity.getAvailableBalance());
            totalUnrealizedPnl = totalUnrealizedPnl.add(equity.getTotalUnrealizedPnl());
            
            if (equity.getBnbBalanceUsdt() != null) {
                totalBnbBalanceUsdt = totalBnbBalanceUsdt.add(equity.getBnbBalanceUsdt());
            }
        }

        Instant latestTimestamp = userEquities.stream()
                .map(equity -> Instant.parse(equity.getTimestamp()))
                .max(Instant::compareTo)
                .orElse(Instant.now());
        
        return UserEquityStateDTO.builder()
                .userId(userId)
                .totalWalletBalance(totalWalletBalance)
                .totalAvailableBalance(totalAvailableBalance)
                .totalUnrealizedPnl(totalUnrealizedPnl)
                .totalBnbBalanceUsdt(totalBnbBalanceUsdt)
                .timestamp(latestTimestamp)
                .venueEquities(userEquities)
                .build();
    }
    
    /**
     * Get all currently stored equity records.
     * 
     * @return List of equity records
     */
    public List<Equity> getAllEquities() {
        return knownEquityKeys.keySet().stream()
                .map(key -> cacheService.getFromCache(key, Equity.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get the known equity cache keys.
     * 
     * @return Set of known equity keys
     */
    public Set<String> getKnownEquityKeys() {
        return knownEquityKeys.keySet();
    }
} 