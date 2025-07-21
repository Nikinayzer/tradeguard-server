package korn03.tradeguardserver.kafka.consumer;

import korn03.tradeguardserver.endpoints.dto.user.risk.RiskReportDTO;
import korn03.tradeguardserver.service.risk.RiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskUpdatesConsumer {
    private final RiskService riskService;

    @KafkaListener(topics = "${kafka.topic.risk-updates}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "riskUpdatesListenerFactory")
    public void consumeRiskUpdate(RiskReportDTO riskReport) {
        try {
            log.debug("Received risk update for user {}: {}", riskReport.getUserId(), riskReport.getEventType());
            riskService.processRiskUpdate(riskReport);
        } catch (Exception e) {
            log.error("Error processing risk update: {}", e.getMessage(), e);
        }
    }
}