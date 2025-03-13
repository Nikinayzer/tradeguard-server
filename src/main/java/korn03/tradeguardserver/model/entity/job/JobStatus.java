package korn03.tradeguardserver.model.entity.job;

import korn03.tradeguardserver.kafka.events.JobEventType;

public enum JobStatus {
    CREATED,
    PAUSED,
    IN_PROGRESS,
    STOPPED,
    FINISHED,
    CANCELLED;

    /**
     * Maps an event type to a job status.
     */
    public static JobStatus fromEventType(JobEventType.JobEventTypeEnum eventType) {
        return switch (eventType) {
            case CREATED -> CREATED;
            case PAUSED -> PAUSED;
            case CANCELED_ORDERS -> CANCELLED;
            case STOPPED -> STOPPED;
            case FINISHED-> FINISHED;
            default -> IN_PROGRESS;
        };
    }
}