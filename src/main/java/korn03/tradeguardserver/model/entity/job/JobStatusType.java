package korn03.tradeguardserver.model.entity.job;

public enum JobStatusType {
    CREATED,
    PAUSED,
    IN_PROGRESS,
    STOPPED,
    CANCELED,
    FINISHED;

    public boolean canPause() {
        return this != PAUSED && this != STOPPED && this != CANCELED && this != FINISHED;
    }

    public boolean canResume() {
        return this == PAUSED;
    }

    public boolean canCancel() {
        return this != CANCELED && this != FINISHED && this != STOPPED;
    }

    public boolean canStop() {
        return this != CANCELED && this != FINISHED && this != STOPPED;
    }

    public boolean isActive() {
        return this != FINISHED && this != CANCELED && this != STOPPED;
    }
}