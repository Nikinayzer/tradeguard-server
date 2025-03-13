package korn03.tradeguardserver.kafka.consumer;

import korn03.tradeguardserver.kafka.events.JobEventMessage;
import korn03.tradeguardserver.service.job.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Service for consuming job events from Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobEventConsumer {

    private final JobService jobService;

    @KafkaListener(topics = "${kafka.topic.jobs}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "jobEventListenerFactory")
    public void consumeJobEvent(JobEventMessage jobEventMessage) {
        if (jobEventMessage.getJobEventType() == null) {
            log.warn("⚠️ Received event with null type: {}", jobEventMessage);
            return;
        }

        jobService.processJobEvent(jobEventMessage);

        switch (jobEventMessage.getJobEventType().getType()) {
            case CREATED -> handleJobCreated(jobEventMessage);
            case STEP_DONE -> handleStepDone(jobEventMessage);
            case PAUSED -> handleJobPaused(jobEventMessage);
            case RESUMED -> handleJobResumed(jobEventMessage);
            case CANCELED_ORDERS -> handleJobCanceledOrders(jobEventMessage);
            case STOPPED -> handleJobStopped(jobEventMessage);
            case FINISHED -> handleJobFinished(jobEventMessage);
            default -> log.warn("⚠️ Unrecognized event type: {}", jobEventMessage.getJobEventType());
        }
    }


    //DEBUG METHODS, WILL BE REMOVED (PROBABLY, FOR NOW LOGIC IS IN SERVICE LAYER)
    private void handleJobCreated(JobEventMessage jobEventMessage) {
        log.info("🚀 Job Created: jobId={}, name={}, coins={}, side={}, discountPct={}, amount={}, stepsTotal={}", jobEventMessage.getJobId(), jobEventMessage.getName(), jobEventMessage.getCoins(), jobEventMessage.getSide(), jobEventMessage.getDiscountPct(), jobEventMessage.getAmount(), jobEventMessage.getStepsTotal());
    }

    private void handleStepDone(JobEventMessage jobEventMessage) {
        log.info("✅ Step Done: jobId={}, step={}", jobEventMessage.getJobId(), jobEventMessage.getStepsDone());
    }

    private void handleJobPaused(JobEventMessage jobEventMessage) {
        log.info("⏸ Job Paused: jobId={}, stepsDone={}", jobEventMessage.getJobId(), jobEventMessage.getStepsDone());
    }

    private void handleJobResumed(JobEventMessage jobEventMessage) {
        log.info("▶️ Job Resumed: jobId={}, stepsDone={}", jobEventMessage.getJobId(), jobEventMessage.getStepsDone());
    }

    private void handleJobCanceledOrders(JobEventMessage jobEventMessage) {
        log.info("❌ Job Canceled Orders: jobId={}, stepsDone={}", jobEventMessage.getJobId(), jobEventMessage.getStepsDone());
    }

    private void handleJobStopped(JobEventMessage jobEventMessage) {
        log.info("🛑 Job Stopped: jobId={}, stepsDone={}", jobEventMessage.getJobId(), jobEventMessage.getStepsDone());
    }

    private void handleJobFinished(JobEventMessage jobEventMessage) {
        log.info("🏁 Job Finished: jobId={}, stepsDone={}", jobEventMessage.getJobId(), jobEventMessage.getStepsDone());
    }
}
