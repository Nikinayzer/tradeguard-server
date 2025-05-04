package korn03.tradeguardserver.endpoints.controller.test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.Set;

import korn03.tradeguardserver.kafka.events.equity.Equity;
import korn03.tradeguardserver.kafka.events.position.Position;
import korn03.tradeguardserver.service.equity.EquityService;
import korn03.tradeguardserver.service.position.PositionService;
import korn03.tradeguardserver.service.event.SseEmitterService;
import korn03.tradeguardserver.service.core.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for generating mock data for testing.
 * Only available in development profiles.
 */
@RestController
@RequestMapping("/test/mock")
@RequiredArgsConstructor
@Slf4j
public class MockDataController {

    private final PositionService positionService;
    private final EquityService equityService;
    private final SseEmitterService sseService;
    private final CacheService cacheService;
    private final Random random = new Random();

    private String formattedNow() {
        Instant now = Instant.now();

        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
                .withZone(ZoneOffset.UTC)
                .format(now);
    }
    /**
     * Generate a mock position update for a user.
     */
    @PostMapping("/positions/{userId}")
    public ResponseEntity<?> generateMockPosition(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "BYBIT") String venue,
            @RequestParam(defaultValue = "BTC") String symbol,
            @RequestParam(defaultValue = "false" ) boolean closed) {
        
        Position position = new Position();
        position.setUserId(userId);
        position.setVenue(venue);
        position.setSymbol(symbol);
        position.setQty(closed ? BigDecimal.ZERO : BigDecimal.valueOf(random.nextDouble() * 2));
        position.setEntryPrice(BigDecimal.valueOf(30000 + random.nextDouble() * 5000));
        position.setMarkPrice(BigDecimal.valueOf(30000 + random.nextDouble() * 5000));
        position.setSide(random.nextBoolean() ? "Buy" : "Sell");
        position.setLeverage(BigDecimal.valueOf(10));
        position.setTimestamp(formattedNow());
        position.setUsdtAmt(position.getQty().multiply(position.getMarkPrice()));
        position.setUnrealizedPnl(BigDecimal.valueOf(random.nextDouble() * 1000 - 500));
        
        log.info("Generated mock position: {}", position);
        positionService.processPositionUpdate(position);
        
        return ResponseEntity.ok().body(Map.of(
            "message", "Mock position generated",
            "position", position
        ));
    }
    
    /**
     * Generate a mock equity update for a user.
     */
    @PostMapping("/equity/{userId}")
    public ResponseEntity<?> generateMockEquity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "BYBIT") String venue) {
        
        Equity equity = new Equity();
        equity.setUserId(userId);
        equity.setVenue(venue);
        equity.setWalletBalance(BigDecimal.valueOf(10000 + random.nextDouble() * 5000));
        equity.setAvailableBalance(BigDecimal.valueOf(5000 + random.nextDouble() * 3000));
        equity.setTotalUnrealizedPnl(BigDecimal.valueOf(random.nextDouble() * 1000 - 500));
        equity.setBnbBalanceUsdt(BigDecimal.valueOf(random.nextDouble() * 500));
        equity.setTimestamp(formattedNow());
        
        log.info("Generated mock equity: {}", equity);
        equityService.processEquityUpdate(equity);
        
        return ResponseEntity.ok().body(Map.of(
            "message", "Mock equity generated",
            "equity", equity
        ));
    }
    
    /**
     * Generate a series of mock updates to simulate real activity.
     */
    @PostMapping("/simulate/{userId}")
    public ResponseEntity<?> simulateActivity(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int updates,
            @RequestParam(defaultValue = "2000") int delayMs) {
        
        // Run in a separate thread to not block the request
        new Thread(() -> {
            try {
                log.info("Starting simulation for user {} with {} updates", userId, updates);
                
                for (int i = 0; i < updates; i++) {
                    // Generate position update
                    Position position = new Position();
                    position.setUserId(userId);
                    position.setVenue("BYBIT");
                    position.setSymbol("BTCUSDT");
                    position.setQty(BigDecimal.valueOf(1 + random.nextDouble()));
                    position.setEntryPrice(BigDecimal.valueOf(30000 + random.nextDouble() * 5000));
                    position.setSide(random.nextBoolean() ? "Buy" : "Sell");
                    position.setMarkPrice(BigDecimal.valueOf(30000 + random.nextDouble() * 5000));
                    position.setLeverage(BigDecimal.valueOf(10));
                    position.setTimestamp(formattedNow());
                    position.setUsdtAmt(position.getQty().multiply(position.getMarkPrice()));
                    position.setUnrealizedPnl(BigDecimal.valueOf(random.nextDouble() * 1000 - 500));
                    
                    positionService.processPositionUpdate(position);
                    log.debug("Sent position update {}/{}", i+1, updates);

                    Equity equity = new Equity();
                    equity.setUserId(userId);
                    equity.setVenue("BYBIT");
                    equity.setWalletBalance(BigDecimal.valueOf(10000 + random.nextDouble() * 5000));
                    equity.setAvailableBalance(BigDecimal.valueOf(5000 + random.nextDouble() * 3000));
                    equity.setTotalUnrealizedPnl(BigDecimal.valueOf(random.nextDouble() * 1000 - 500));
                    equity.setBnbBalanceUsdt(BigDecimal.valueOf(random.nextDouble() * 500));
                    equity.setTimestamp(formattedNow());
                    
                    equityService.processEquityUpdate(equity);
                    log.debug("Sent equity update {}/{}", i+1, updates);
                    
                    // Sleep between updates
                    Thread.sleep(delayMs);
                }
                
                log.info("Simulation completed for user {}", userId);
            } catch (Exception e) {
                log.error("Error in simulation", e);
            }
        }).start();
        
        return ResponseEntity.accepted().body(Map.of(
            "message", "Simulation started",
            "updates", updates,
            "delayMs", delayMs
        ));
    }
} 