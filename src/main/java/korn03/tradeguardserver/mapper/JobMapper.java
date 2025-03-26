package korn03.tradeguardserver.mapper;

import korn03.tradeguardserver.kafka.events.JobEventMessage;
import korn03.tradeguardserver.kafka.events.JobEventType;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.job.JobStatusType;
import korn03.tradeguardserver.model.entity.job.JobStrategyType;
import org.mapstruct.*;

import java.util.Arrays;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", expression = "java(2L)") // TODO: Accept later
    @Mapping(target = "status", source = "jobEventType", qualifiedByName = "mapStatus")
    @Mapping(target = "stepsDone", expression = "java((jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.JobEventType.StepDone stepDone) ? stepDone.stepIndex() : 0)")
    @Mapping(target = "strategy", source = "name", qualifiedByName = "mapStrategy")
    @Mapping(target = "coins", source = "coins")
    @Mapping(target = "side", source = "side")
    @Mapping(target = "discountPct", source = "discountPct")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "stepsTotal", source = "stepsTotal")
    @Mapping(target = "durationMinutes", source = "durationMinutes")
    @Mapping(target = "createdAt", source = "timestamp")
    @Mapping(target = "updatedAt", source = "timestamp")
    Job toEntity(JobEventMessage jobEventMessage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobId", source = "jobId")
    @Mapping(target = "eventType", source = "jobEventType")
    @Mapping(target = "stepsDone", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.JobEventType.StepDone stepDone ? stepDone.stepIndex() : 0)")
    @Mapping(target = "durationMinutes", source = "durationMinutes")
    @Mapping(target = "timestamp", source = "timestamp")
    JobEvent toJobEvent(JobEventMessage jobEventMessage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobId", ignore = true)
    @Mapping(target = "stepsDone", expression = "java((jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.JobEventType.StepDone stepDone) ? stepDone.stepIndex() : job.getStepsDone())")    @Mapping(target = "status", source = "jobEventType", qualifiedByName = "mapStatus")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", source = "timestamp")
    @Mapping(target = "userId", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExistingJob(@MappingTarget Job job, JobEventMessage jobEventMessage);

    @Named("mapStrategy")
    default JobStrategyType mapStrategy(String name) {
        if (name == null) return null;
        return Arrays.stream(JobStrategyType.values())
                .filter(strategy -> strategy.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Named("mapStatus")
    default JobStatusType mapStatus(JobEventType eventType) {
        return switch (eventType) {
            case JobEventType.Created ignored        -> JobStatusType.CREATED;
            case JobEventType.Paused ignored         -> JobStatusType.PAUSED;
            case JobEventType.CanceledOrders ignored -> JobStatusType.CANCELED;
            case JobEventType.Stopped ignored        -> JobStatusType.STOPPED;
            case JobEventType.Finished ignored       -> JobStatusType.FINISHED;
            default                                  -> JobStatusType.IN_PROGRESS;
        };
    }
}
