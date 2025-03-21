package korn03.tradeguardserver.kafka.producer;

import korn03.tradeguardserver.kafka.events.JobSubmissionMessage;
import korn03.tradeguardserver.kafka.events.DcaJobSubmissionMessage;
import korn03.tradeguardserver.kafka.events.LiqJobSubmissionMessage;
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

    private final KafkaTemplate<String, JobSubmissionMessage> kafkaTemplate;

    @Value("${kafka.topic.job-submissions}")
    private String jobSubmissionsTopic;

    /**
     * Sends a job submission message to Kafka using Spring messaging abstraction
     * @param jobSubmission The job submission message
     * @return CompletableFuture of the send result
     */
    @KafkaHandler
    public CompletableFuture<SendResult<String, JobSubmissionMessage>> sendJobSubmission(JobSubmissionMessage jobSubmission) {
        validateMessage(jobSubmission);

        if (jobSubmission.getTimestamp() == null) {
            jobSubmission.setTimestamp(Instant.now().toEpochMilli());
        }

        String key = jobSubmission.getUserId().toString();

        Message<JobSubmissionMessage> message = MessageBuilder
            .withPayload(jobSubmission)
            .setHeader(KafkaHeaders.TOPIC, jobSubmissionsTopic)
            .setHeader(KafkaHeaders.KEY, key)
            .setHeader(KafkaHeaders.TIMESTAMP, Instant.now().toEpochMilli())
            .build();

        CompletableFuture<SendResult<String, JobSubmissionMessage>> future = 
            kafkaTemplate.send(message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                String messageType = jobSubmission instanceof DcaJobSubmissionMessage ? "DCA" :
                                   jobSubmission instanceof LiqJobSubmissionMessage ? "LIQ" : "BASE";
                log.info("✅ {} job submission sent successfully: userId={}, source={}, coins={}, partition={}, offset={}", 
                    messageType,
                    jobSubmission.getUserId(), 
                    jobSubmission.getSource(), 
                    jobSubmission.getCoins(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
            } else {
                log.error("❌ Failed to send job submission: userId={}, error={}", 
                    jobSubmission.getUserId(), ex.getMessage(), ex);
            }
        });

        return future;
    }

    private void validateMessage(JobSubmissionMessage message) {
        if (message.getUserId() == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (message.getSource() == null || message.getSource().isEmpty()) {
            throw new IllegalArgumentException("source cannot be null or empty");
        }
        if (message.getCoins() == null || message.getCoins().isEmpty()) {
            throw new IllegalArgumentException("coins cannot be null or empty");
        }
        if (message.getTotalSteps() == null || message.getTotalSteps() <= 0) {
            throw new IllegalArgumentException("totalSteps must be positive");
        }
        if (message.getSide() == null || message.getSide().isEmpty()) {
            throw new IllegalArgumentException("side cannot be null or empty");
        }
        if (message.getDiscountPct() == null || message.getDiscountPct() < 0) {
            throw new IllegalArgumentException("discountPct must be non-negative");
        }
        if (message.getRandomnessPct() == null || message.getRandomnessPct() < 0) {
            throw new IllegalArgumentException("randomnessPct must be non-negative");
        }

        if (message instanceof DcaJobSubmissionMessage dca) {
            if (dca.getTotalAmt() == null || dca.getTotalAmt() <= 0) {
                throw new IllegalArgumentException("totalAmt must be positive for DCA jobs");
            }
        }

        if (message instanceof LiqJobSubmissionMessage liq) {
            if (liq.getProportionPct() == null || liq.getProportionPct() < 0 || liq.getProportionPct() > 100) {
                throw new IllegalArgumentException("proportionPct must be between 0 and 100 for LIQ jobs");
            }
        }
    }
} 