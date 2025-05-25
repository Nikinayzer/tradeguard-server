package korn03.tradeguardserver.kafka.consumer;

import korn03.tradeguardserver.kafka.events.position.PositionKafkaDTO;
import korn03.tradeguardserver.service.position.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka positions consumer
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PositionsConsumer {

    private final PositionService positionService;

    @KafkaListener(topics = "${kafka.topic.position-updates}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "positionListenerFactory")
    public void consumePositionUpdate(PositionKafkaDTO position) {
        log.info("Received position update: {}", position);
        positionService.processPositionUpdate(position);
    }
}
