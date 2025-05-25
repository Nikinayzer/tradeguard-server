package korn03.tradeguardserver.service.risk;

import korn03.tradeguardserver.endpoints.dto.user.risk.RiskReportDTO;
import korn03.tradeguardserver.service.core.CacheService;
import korn03.tradeguardserver.service.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {
    private static final String RISK_KEY_PREFIX = "risk:";
    private static final Duration RISK_TTL = Duration.ofHours(24);
    
    private final SseEmitterService sseEmitterService;
    private final CacheService cacheService;

    public void processRiskUpdate(RiskReportDTO riskReport) {
        if (riskReport == null || riskReport.getUserId() == null) {
            log.warn("Received null risk report or user ID");
            return;
        }

        try {
            String key = RISK_KEY_PREFIX + riskReport.getUserId();
            cacheService.storeInCache(key, riskReport, RISK_TTL);
            log.info("Stored risk report for user {} with TTL {}", riskReport.getUserId(), RISK_TTL);
            
            // Send SSE update
            sseEmitterService.sendUpdate(riskReport.getUserId(), "risk_report", riskReport);
            log.info("Sent risk report update via SSE for user {}", riskReport.getUserId());
            
        } catch (Exception e) {
            log.error("Error processing risk update for user {}: {}", riskReport.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * Get the current risk report for a user.
     * 
     * @param userId The user ID
     * @return The current risk report, or null if none exists
     */
    public RiskReportDTO getUserRiskReport(Long userId) {
        if (userId == null) {
            log.warn("Attempted to get risk report for null user ID");
            return null;
        }

        try {
            String key = RISK_KEY_PREFIX + userId;
            RiskReportDTO riskReport = cacheService.getFromCache(key, RiskReportDTO.class);
            
            if (riskReport != null) {
                log.debug("Retrieved risk report for user {}", userId);
            } else {
                log.debug("No risk report found for user {}", userId);
            }
            
            return riskReport;
        } catch (Exception e) {
            log.error("Error retrieving risk report for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
} 