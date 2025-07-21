package korn03.tradeguardserver.service.risk;

import korn03.tradeguardserver.endpoints.dto.user.risk.RiskLevel;
import korn03.tradeguardserver.endpoints.dto.user.risk.RiskReportDTO;
import korn03.tradeguardserver.model.entity.service.notifications.NotificationCategory;
import korn03.tradeguardserver.model.entity.service.notifications.NotificationType;
import korn03.tradeguardserver.service.core.CacheService;
import korn03.tradeguardserver.service.core.pushNotifications.PushNotificationService;
import korn03.tradeguardserver.service.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskService {
    private static final String RISK_KEY_PREFIX = "risk:";
    private static final Duration RISK_TTL = Duration.ofHours(24);

    private final SseEmitterService sseEmitterService;
    private final CacheService cacheService;
    private final PushNotificationService pushNotificationService;

    public void processRiskUpdate(RiskReportDTO riskReport) {
        if (riskReport == null || riskReport.getUserId() == null) {
            log.warn("Received null risk report or user ID");
            return;
        }

        try {
            String key = RISK_KEY_PREFIX + riskReport.getUserId();
            cacheService.storeInCache(key, riskReport, RISK_TTL);
            log.info("Stored risk report for user {} with TTL {}", riskReport.getUserId(), RISK_TTL);

            sendRiskPushNotification(
                    riskReport.getUserId(),
                    riskReport.getTopRiskLevel(),
                    riskReport.getTopRiskType().name(),
                    riskReport.getTopRiskConfidence()
            );
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

    private void sendRiskPushNotification(Long userId, RiskLevel topRiskLevel, String TopCategory, Double topRiskConfidence) {
        String title;
        String message;
        NotificationType notificationType = NotificationType.WARNING;
        switch (topRiskLevel) {
            case RiskLevel.CRITICAL ->{
                title = String.format("Critical Risk Alert for %s", TopCategory);
                message = String.format("Critical risk detected for your account! Category: %s, Confidence: %.2f%%", TopCategory, topRiskConfidence * 100);
            }
            case RiskLevel.HIGH -> {
                title = String.format("High Risk Alert for %s", TopCategory);
                message = String.format("High risk detected for your account! Category: %s, Confidence: %.2f%%", TopCategory, topRiskConfidence * 100);
            }
            case RiskLevel.MEDIUM -> {
                title = String.format("Medium Risk Alert for %s", TopCategory);
                message = String.format("Medium risk detected for your account! Category: %s, Confidence: %.2f%%", TopCategory, topRiskConfidence * 100);
            }
            case RiskLevel.NONE -> {
                return;
            }
            default -> {
                title = String.format("Low Risk Alert for %s", TopCategory);
                message = String.format("Low risk detected for your account! Category: %s, Confidence: %.2f%%", TopCategory, topRiskConfidence * 100);
            }
        }
        pushNotificationService.sendPushNotification(
                userId,
                NotificationCategory.HEALTH,
                notificationType,
                title,
                message
        );
    }
}