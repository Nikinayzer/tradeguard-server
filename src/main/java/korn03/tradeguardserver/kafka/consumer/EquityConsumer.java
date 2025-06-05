package korn03.tradeguardserver.kafka.consumer;

import korn03.tradeguardserver.kafka.events.equity.EquityKafkaDTO;
import korn03.tradeguardserver.service.equity.EquityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka equity consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquityConsumer {

    private final EquityService equityService;

    @KafkaListener(topics = "${kafka.topic.equity}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "equityListenerFactory")
    public void consumeEquityUpdate(EquityKafkaDTO equity) {
        log.debug("Received equity update for user {} at venue {}", equity.getUserId(), equity.getVenue());
        equityService.processEquityUpdate(equity);
    }
}
