package korn03.tradeguardserver.service.job;

import korn03.tradeguardserver.endpoints.dto.user.job.DcaJobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.LiqJobSubmissionDTO;
import korn03.tradeguardserver.exception.NotFoundException;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobSubmissionKafkaDTO;
import korn03.tradeguardserver.kafka.producer.JobSubmissionProducer;
import korn03.tradeguardserver.mapper.JobMapper;
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
    private final JobMapper jobMapper;

    public CompletableFuture<Void> sendCreatedDcaJob(DcaJobSubmissionDTO dto, Long userId) {
        JobSubmissionKafkaDTO kafkaDTO = jobMapper.toKafkaDTO(dto);
        kafkaDTO.setUserId(userId);
        return producer.sendJobSubmission(kafkaDTO).thenAccept(result -> log.info("✅ DCA job creation sent: {}", result.getRecordMetadata()));
    }

    public CompletableFuture<Void> sendCreatedLiqJob(LiqJobSubmissionDTO dto, Long userId) {
        JobSubmissionKafkaDTO kafkaDTO = jobMapper.toKafkaDTO(dto);
        kafkaDTO.setUserId(userId);

        return producer.sendJobSubmission(kafkaDTO).thenAccept(result -> log.info("✅ LIQ job creation sent: {}", result.getRecordMetadata()));
    }

    public void sendPauseEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canPause()) {
            log.warn("Job {} cannot be paused in current state: {}", jobId, job.getStatus());
            throw new IllegalStateException("Job cannot be paused in current state: " + job.getStatus());
        }
        sendSimpleEvent(jobId, new JobEventType.Paused(), source);
    }

    public void sendResumeEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canResume()) {
            log.warn("Job {} cannot be resumed in current state: {}", jobId, job.getStatus());
            throw new IllegalStateException("Job cannot be resumed in current state: " + job.getStatus());
        }
        sendSimpleEvent(jobId, new JobEventType.Resumed(), source);
    }

    public void sendCancelEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canCancel()) {
            log.warn("Job {} cannot be canceled in current state: {}", jobId, job.getStatus());
            throw new IllegalStateException("Job cannot be canceled in current state: " + job.getStatus());
        }

        sendSimpleEvent(jobId, new JobEventType.CanceledOrders(), source);
    }

    public void sendStopEvent(Long jobId, Long userId, String source) {
        Job job = verifyOwnership(jobId, userId);
        if (!job.getStatus().canStop()) {
            log.warn("Job {} cannot be stopped in current state: {}", jobId, job.getStatus());
            throw new IllegalStateException("Job cannot be stopped in current state: " + job.getStatus());
        }

        sendSimpleEvent(jobId, new JobEventType.Stopped(), source);
    }

    private void sendSimpleEvent(Long jobId, JobEventType eventType, String source) {
        JobEventMessage message = JobEventMessage.builder()
                .jobId(jobId)
                .jobEventType(eventType)
                .source(source)
                .timestamp(Instant.now())
                .build();

        //TODO decide of another topic is needed
        //producer.sendJobSubmission(message).thenAccept(result -> log.info("✅ {} event sent for job {}: {}", eventType.getClass().getSimpleName(), jobId, result.getRecordMetadata()));
    }

    private Job verifyOwnership(Long jobId, Long userId) {
        return jobService.getJobById(jobId).filter(job -> job.getUserId().equals(userId)).orElseThrow(() -> new NotFoundException("Job not found"));
    }
}

