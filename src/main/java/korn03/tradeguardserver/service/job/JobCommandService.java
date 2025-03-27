package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.endpoints.dto.user.job.JobSubmissionDTO;
import korn03.tradeguardserver.kafka.events.JobEventMessage;
import korn03.tradeguardserver.kafka.events.JobEventType;
import korn03.tradeguardserver.kafka.producer.JobSubmissionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobCommandService {

    private final JobSubmissionProducer producer;

    public CompletableFuture<Void> sendCreatedJob(String strategy, JobSubmissionDTO dto, Long userId) {
        JobEventType.CreatedMeta meta = new JobEventType.CreatedMeta(
                strategy, userId, dto.getCoins(), dto.getSide(),
                dto.getDiscountPct(), dto.getAmount(),
                dto.getTotalSteps(), dto.getDurationMinutes()
        );

        JobEventMessage message = JobEventMessage.builder()
                .jobId(null)
//                .userId(userId)
                .jobEventType(new JobEventType.Created(meta))
                .source(dto.getSource())
                .timestamp(Instant.now())
                .build();

        return producer.sendJobEvent(message)
                .thenAccept(result -> log.info("✅ Job creation sent: {}", result.getRecordMetadata()));
    }

    public CompletableFuture<Void> sendSimpleEvent(Long jobId, JobEventType eventType, String source) {
        JobEventMessage message = JobEventMessage.builder()
                .jobId(jobId)
                .jobEventType(eventType)
                .source(source)
                .timestamp(Instant.now())
                .build();

        return producer.sendJobEvent(message)
                .thenAccept(result -> log.info("✅ {} event sent for job {}: {}", eventType.getClass().getSimpleName(), jobId, result.getRecordMetadata()));
    }
}

