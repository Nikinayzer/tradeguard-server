package korn03.tradeguardserver.service.equity;

import korn03.tradeguardserver.kafka.events.equity.EquityKafkaDTO;
import korn03.tradeguardserver.endpoints.dto.user.equity.UserEquityStateDTO;
import korn03.tradeguardserver.mapper.EquityMapper;
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
 * Service for managing equity data from Kafka in Redis cache.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EquityService {

    private final CacheService cacheService;
    private final SseEmitterService sseService;
    private final EquityMapper equityMapper;
    private static final Duration EQUITY_TTL = Duration.ofMinutes(1);
    private static final String EQUITY_KEY_PREFIX = "equity:";
    
    // In-memory map to track known equity keys
    private final Map<String, Boolean> knownEquityKeys = new ConcurrentHashMap<>();

    /**
     * Process and store an equity update received from Kafka.
     * 
     * @param equity The equity data to store
     */
    public void processEquityUpdate(EquityKafkaDTO equity) {
        if (equity == null || equity.getEquity() == null) {
            log.warn("Received null equity update or equity details");
            return;
        }
        
        try {
            String key = EQUITY_KEY_PREFIX + equity.getUserId() + ":" + equity.getVenue();
            cacheService.storeInCache(key, equity, EQUITY_TTL);
            knownEquityKeys.put(key, true);
            log.debug("Stored equity update for key: {}", key);
            
            UserEquityStateDTO equityState = getUserEquityState(equity.getUserId());
            if (equityState != null) {
                log.debug("Sending equity update via SSE for user {}", equity.getUserId());
                sseService.sendUpdate(equity.getUserId(), "equity", equityState);
            } else {
                log.warn("Could not generate equity state for user {}", equity.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing equity update for {}: {}", equity.getVenue(), e.getMessage(), e);
        }
    }
    
    /**
     * Get a specific equity record by user and venue.
     * 
     * @param userId The user ID
     * @param venue The trading venue
     * @return The equity record if found, null otherwise
     */
    public EquityKafkaDTO getEquity(Long userId, String venue) {
        String cacheKey = EQUITY_KEY_PREFIX + userId + ":" + venue;
        return cacheService.getFromCache(cacheKey, EquityKafkaDTO.class);
    }
    
    /**
     * Get all equity records for a specific user.
     * 
     * @param userId The user ID
     * @return List of equity records for the user
     */
    public List<EquityKafkaDTO> getUserEquities(Long userId) {
        String userKeyPrefix = EQUITY_KEY_PREFIX + userId + ":";
        List<EquityKafkaDTO> userEquities = new ArrayList<>();
        
        for (String key : knownEquityKeys.keySet()) {
            if (key.startsWith(userKeyPrefix)) {
                EquityKafkaDTO equity = cacheService.getFromCache(key, EquityKafkaDTO.class);
                if (equity != null) {
                    userEquities.add(equity);
                    log.debug("Retrieved equity from cache: {}", key);
                } else {
                    log.warn("Equity not found in cache for key: {}", key);
                }
            }
        }
        
        log.debug("Found {} equity records for user {}", userEquities.size(), userId);
        return userEquities;
    }
    
    /**
     * Get a consolidated view of a user's equity across all venues.
     * 
     * @param userId The user ID
     * @return Combined equity state DTO or null if no equity records exist
     */
    public UserEquityStateDTO getUserEquityState(Long userId) {
        List<EquityKafkaDTO> userEquities = getUserEquities(userId);
        
        if (userEquities.isEmpty()) {
            return null;
        }
        
        EquityTotals totals = calculateEquityTotals(userEquities);
        Instant latestTimestamp = findLatestTimestamp(userEquities);
        
        return UserEquityStateDTO.builder()
                .summary(UserEquityStateDTO.EquitySummary.builder()
                    .totalWalletBalance(totals.totalWalletBalance)
                    .totalAvailableBalance(totals.totalAvailableBalance)
                    .totalUnrealizedPnl(totals.totalUnrealizedPnl)
                    .totalBnbBalance(totals.totalBnbBalance)
                    .lastUpdate(latestTimestamp)
                    .build())
                .venueEquities(equityMapper.toFrontendDTOList(userEquities))
                .build();
    }
    
    /**
     * Get all currently stored equity records.
     * 
     * @return List of equity records
     */
    public List<EquityKafkaDTO> getAllEquities() {
        return knownEquityKeys.keySet().stream()
                .map(key -> cacheService.getFromCache(key, EquityKafkaDTO.class))
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

    private EquityTotals calculateEquityTotals(List<EquityKafkaDTO> equities) {
        BigDecimal totalWalletBalance = BigDecimal.ZERO;
        BigDecimal totalAvailableBalance = BigDecimal.ZERO;
        BigDecimal totalUnrealizedPnl = BigDecimal.ZERO;
        BigDecimal totalBnbBalance = BigDecimal.ZERO;
        
        for (EquityKafkaDTO equity : equities) {
            EquityKafkaDTO.EquityDetailsKafkaDTO details = equity.getEquity();
            if (details != null) {
                if (details.getWalletBalance() != null) {
                    totalWalletBalance = totalWalletBalance.add(details.getWalletBalance());
                }
                if (details.getAvailableBalance() != null) {
                    totalAvailableBalance = totalAvailableBalance.add(details.getAvailableBalance());
                }
                if (details.getTotalUnrealizedPnl() != null) {
                    totalUnrealizedPnl = totalUnrealizedPnl.add(details.getTotalUnrealizedPnl());
                }
                if (details.getBnbBalanceUsdt() != null) {
                    totalBnbBalance = totalBnbBalance.add(details.getBnbBalanceUsdt());
                }
            }
        }
        
        return new EquityTotals(totalWalletBalance, totalAvailableBalance, totalUnrealizedPnl, totalBnbBalance);
    }

    private Instant findLatestTimestamp(List<EquityKafkaDTO> equities) {
        return equities.stream()
                .map(equity -> Instant.parse(equity.getTimestamp()))
                .max(Instant::compareTo)
                .orElse(Instant.now());
    }

    @lombok.Value
    private static class EquityTotals {
        BigDecimal totalWalletBalance;
        BigDecimal totalAvailableBalance;
        BigDecimal totalUnrealizedPnl;
        BigDecimal totalBnbBalance;
    }
} 