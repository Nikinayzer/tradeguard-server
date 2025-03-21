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
    @Mapping(target = "userId", expression = "java(1L)") // TODO: Accept later
    @Mapping(target = "status", source = "jobEventType", qualifiedByName = "mapStatus")
    @Mapping(target = "stepsDone", source = "stepsDone")
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
    @Mapping(target = "eventType", source = "jobEventType")
    @Mapping(target = "timestamp", source = "timestamp")
    JobEvent toJobEvent(JobEventMessage jobEventMessage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobId", ignore = true)
    @Mapping(target = "stepsDone", source = "stepsDone")
    @Mapping(target = "status", source = "jobEventType", qualifiedByName = "mapStatus")
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
            case CREATED -> JobStatusType.CREATED;
            case PAUSED -> JobStatusType.PAUSED;
            case CANCELED_ORDERS -> JobStatusType.CANCELED;
            case STOPPED -> JobStatusType.STOPPED;
            case FINISHED -> JobStatusType.FINISHED;
            default -> JobStatusType.IN_PROGRESS;
        };
    }
}
