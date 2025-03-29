package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.endpoints.dto.user.job.DcaJobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.LiqJobSubmissionDTO;
import korn03.tradeguardserver.exception.NotFoundException;
import korn03.tradeguardserver.kafka.events.JobEventMessage;
import korn03.tradeguardserver.kafka.events.JobEventType;
import korn03.tradeguardserver.kafka.producer.JobSubmissionProducer;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.repository.job.JobRepository;
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
    private final JobRepository jobRepository;
    private final JobService jobService;

    public CompletableFuture<Void> sendCreatedDcaJob(DcaJobSubmissionDTO dto, Long userId) {
        JobEventType.CreatedMeta meta = new JobEventType.CreatedMeta(
                "Dca",
                userId,
                dto.getCoins(),
                dto.getSide(),
                dto.getDiscountPct(),
                dto.getAmount(),
                dto.getTotalSteps(),
                dto.getDurationMinutes());

        JobEventMessage message = JobEventMessage.builder().jobId(null).jobEventType(new JobEventType.Created(meta)).source(dto.getSource()).timestamp(Instant.now()).build();

        return producer.sendJobEvent(message).thenAccept(result -> log.info("✅ DCA job creation sent: {}", result.getRecordMetadata()));
    }

    public CompletableFuture<Void> sendCreatedLiqJob(LiqJobSubmissionDTO dto, Long userId) {
        JobEventType.CreatedMeta meta = new JobEventType.CreatedMeta(
                "Liq",
                userId,
                dto.getCoins(),
                dto.getSide(),
                dto.getDiscountPct(),
                dto.getAmount(),
                dto.getTotalSteps(),
                dto.getDurationMinutes());

        JobEventMessage message = JobEventMessage.builder().jobId(null).jobEventType(new JobEventType.Created(meta)).source(dto.getSource()).timestamp(Instant.now()).build();

        return producer.sendJobEvent(message).thenAccept(result -> log.info("✅ LIQ job creation sent: {}", result.getRecordMetadata()));
    }

    public CompletableFuture<Void> sendPauseEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canPause()) {
            throw new IllegalStateException("Job cannot be paused in current state: " + job.getStatus());
        }
        return sendSimpleEvent(jobId, new JobEventType.Paused(), source);
    }

    public CompletableFuture<Void> sendResumeEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canResume()) {
            throw new IllegalStateException("Job cannot be resumed in current state: " + job.getStatus());
        }
        return sendSimpleEvent(jobId, new JobEventType.Resumed(), source);
    }

    public CompletableFuture<Void> sendCancelEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canCancel()) {
            throw new IllegalStateException("Job cannot be canceled in current state: " + job.getStatus());
        }

        return sendSimpleEvent(jobId, new JobEventType.CanceledOrders(), source);
    }

    public CompletableFuture<Void> sendStopEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canStop()) {
            throw new IllegalStateException("Job cannot be stopped in current state: " + job.getStatus());
        }

        return sendSimpleEvent(jobId, new JobEventType.Stopped(), source);
    }

    private CompletableFuture<Void> sendSimpleEvent(Long jobId, JobEventType eventType, String source) {
        JobEventMessage message = JobEventMessage.builder().jobId(jobId).jobEventType(eventType).source(source).timestamp(Instant.now()).build();

        return producer.sendJobEvent(message).thenAccept(result -> log.info("✅ {} event sent for job {}: {}", eventType.getClass().getSimpleName(), jobId, result.getRecordMetadata()));
    }

    private Job verifyOwnership(Long jobId, Long userId) {
        return jobService.getJobById(jobId).filter(job -> job.getUserId().equals(userId)).orElseThrow(() -> new NotFoundException("Job not found"));
    }
}

