package korn03.tradeguardserver.kafka.consumer;

import korn03.tradeguardserver.kafka.events.position.Position;
import korn03.tradeguardserver.service.position.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service for consuming position updates from Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PositionsConsumer {

    private final PositionService positionService;

    @KafkaListener(topics = "${kafka.topic.position-updates}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "positionListenerFactory")
    public void consumePositionUpdate(Position position) {
        log.info("Received position update: {}", position);
        positionService.processPositionUpdate(position);
    }
}
