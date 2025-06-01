package korn03.tradeguardserver.kafka.consumer;

import jakarta.annotation.PostConstruct;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobHistoricalConsumer {

    private final JobService jobService;
    private final KafkaConsumer<String, JobEventMessage> replayKafkaConsumer;

    @Value("${kafka.topic.job-updates}")
    private String jobUpdatesTopic;

    @PostConstruct
    public void consumeHistoryAtStartup() {
        log.info("Replaying historical job events from Kafka...");

        replayKafkaConsumer.subscribe(List.of(jobUpdatesTopic));
        while (replayKafkaConsumer.assignment().isEmpty()) {
            replayKafkaConsumer.poll(Duration.ofMillis(100));
        }

        replayKafkaConsumer.seekToBeginning(replayKafkaConsumer.assignment());

        int messageCount = 0;

        while (true) {
            var records = replayKafkaConsumer.poll(Duration.ofSeconds(1));
            if (records.isEmpty()) break;

            for (var record : records) {
                jobService.processHistoricalJobEvent(record.value());
                messageCount++;
            }
        }

        replayKafkaConsumer.close();
        log.info("Replay completed. Processed {} messages", messageCount);
    }
}