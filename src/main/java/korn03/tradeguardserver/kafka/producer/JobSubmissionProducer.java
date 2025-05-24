package korn03.tradeguardserver.kafka.producer;

import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobSubmissionKafkaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.support.KafkaHeaders;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSubmissionProducer {

    private final KafkaTemplate<String, JobSubmissionKafkaDTO> kafkaTemplate;

    @Value("${kafka.topic.job-submissions}")
    private String jobSubmissionsTopic;

    /**
     * Sends a job event message (Created, Paused, etc.) to Kafka.
     * Used for submitting jobs or triggering state changes.
     */
    @KafkaHandler
    public CompletableFuture<SendResult<String, JobSubmissionKafkaDTO>> sendJobSubmission(JobSubmissionKafkaDTO jobEventMessage) {
        String key = resolveKafkaKey(jobEventMessage);
        assert key != null;
        Message<JobSubmissionKafkaDTO> kafkaMessage = MessageBuilder
                .withPayload(jobEventMessage)
                .setHeader(KafkaHeaders.TOPIC, jobSubmissionsTopic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.TIMESTAMP, Instant.now().toEpochMilli())
                .build();
        log.info(kafkaMessage.toString());
        CompletableFuture<SendResult<String, JobSubmissionKafkaDTO>> future = kafkaTemplate.send(kafkaMessage);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Job submission sent successfully: userId={}, strategy={}, partition={}, offset={}",
                        jobEventMessage.getUserId(),
                        jobEventMessage.getStrategy(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error("❌ Failed to send job event: userId={}, strategy={}, error={}",
                        jobEventMessage.getUserId(),
                        jobEventMessage.getStrategy(),
                        ex.getMessage(), ex
                );
            }
        });

        return future;
    }
    private String resolveKafkaKey(JobSubmissionKafkaDTO msg) {
        String strategy = msg.getStrategy();
        return String.valueOf(strategy+"-submission-" + msg.getUserId());
    }

} 