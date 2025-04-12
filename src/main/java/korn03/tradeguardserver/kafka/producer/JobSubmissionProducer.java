package korn03.tradeguardserver.kafka.producer;

import korn03.tradeguardserver.kafka.events.JobEventMessage;
import korn03.tradeguardserver.kafka.events.JobEventType;
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

    private final KafkaTemplate<String, JobEventMessage> kafkaTemplate;

    @Value("${kafka.topic.job-submissions}")
    private String jobSubmissionsTopic;

    /**
     * Sends a job event message (Created, Paused, etc.) to Kafka.
     * Used for submitting jobs or triggering state changes.
     */
    @KafkaHandler
    public CompletableFuture<SendResult<String, JobEventMessage>> sendJobEvent(JobEventMessage jobEventMessage) {
        String key = resolveKafkaKey(jobEventMessage);
        assert key != null;
        Message<JobEventMessage> kafkaMessage = MessageBuilder
                .withPayload(jobEventMessage)
                .setHeader(KafkaHeaders.TOPIC, jobSubmissionsTopic)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(KafkaHeaders.TIMESTAMP, Instant.now().toEpochMilli())
                .build();
        log.info(kafkaMessage.toString());
        CompletableFuture<SendResult<String, JobEventMessage>> future = kafkaTemplate.send(kafkaMessage);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Job event sent successfully: type={}, jobId={}, partition={}, offset={}",
                        jobEventMessage.getJobEventType().getClass().getSimpleName(),
                        jobEventMessage.getJobId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            } else {
                log.error("❌ Failed to send job event: type={}, jobId={}, error={}",
                        jobEventMessage.getJobEventType().getClass().getSimpleName(),
                        jobEventMessage.getJobId(),
                        ex.getMessage(), ex
                );
            }
        });

        return future;
    }
    private String resolveKafkaKey(JobEventMessage msg) {
        JobEventType type = msg.getJobEventType();

        if (type instanceof JobEventType.Created created) {
            return String.valueOf(created.meta().userId());
        }

        if (msg.getJobId() != null) {
            return String.valueOf(msg.getJobId());
        }

        return null;
    }

} 