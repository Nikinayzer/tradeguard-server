package korn03.tradeguardserver.kafka.consumer;

import korn03.tradeguardserver.kafka.events.equity.Equity;
import korn03.tradeguardserver.service.equity.EquityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service for consuming equity updates from Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EquityConsumer {

    private final EquityService equityService;

    @KafkaListener(topics = "${kafka.topic.equity}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "equityListenerFactory")
    public void consumeEquityUpdate(Equity equity) {
        log.info("Received equity update: {}", equity);
        equityService.processEquityUpdate(equity);
    }
}
