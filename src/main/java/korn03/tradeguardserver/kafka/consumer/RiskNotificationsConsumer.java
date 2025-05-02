package korn03.tradeguardserver.kafka.consumer;

import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskNotificationsConsumer {
    private final JobService jobService;

    @KafkaListener(topics = "${kafka.topic.position-updates}", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "jobEventListenerFactory")
    public void consumeJobEvent(JobEventMessage jobEventMessage) {
        jobService.processJobEvent(jobEventMessage);
    }
}
