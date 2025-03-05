package korn03.tradeguardserver.kafka;

import korn03.tradeguardserver.db.entity.CommandLog;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommandLogKafkaProducer {

    private final KafkaTemplate<String, CommandLog> kafkaTemplate;

    public CommandLogKafkaProducer(KafkaTemplate<String, CommandLog> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCommandLog(CommandLog log) {
        kafkaTemplate.send("command-logs", log);
    }
}
