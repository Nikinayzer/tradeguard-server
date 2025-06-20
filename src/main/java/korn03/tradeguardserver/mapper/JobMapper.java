package korn03.tradeguardserver.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import korn03.tradeguardserver.endpoints.dto.user.job.DcaJobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.JobDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.JobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.LiqJobSubmissionDTO;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventMessage;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType;
import korn03.tradeguardserver.kafka.events.jobUpdates.JobSubmissionKafkaDTO;
import korn03.tradeguardserver.model.entity.job.Job;
import korn03.tradeguardserver.model.entity.job.JobEvent;
import korn03.tradeguardserver.model.entity.job.JobStatusType;
import korn03.tradeguardserver.model.entity.job.JobStrategyType;
import org.mapstruct.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {

    //@Mapping(target = "id", ignore = true)
    @Mapping(target = "jobId", source = "jobId")
    @Mapping(target = "userId", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? ((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().userId() : null)")
    @Mapping(target = "status", source = "jobEventType", qualifiedByName = "mapStatus")
    @Mapping(target = "stepsDone", expression = "java((jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.StepDone stepDone) ? stepDone.stepIndex() : 0)")
    @Mapping(target = "strategy", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? mapStrategy(((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().name()) : null)")
    @Mapping(target = "coins", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? ((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().coins() : null)")
    @Mapping(target = "side", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? ((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().side() : null)")
    @Mapping(target = "discountPct", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? ((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().discountPct() : null)")
    @Mapping(target = "amount", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? ((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().amount() : null)")
    @Mapping(target = "stepsTotal", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? ((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().stepsTotal() : null)")
    @Mapping(target = "durationMinutes", expression = "java(jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created ? ((korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.Created) jobEventMessage.getJobEventType()).meta().durationMinutes() : null)")
    @Mapping(target = "createdAt", source = "timestamp")
    @Mapping(target = "updatedAt", source = "timestamp")
    Job toEntity(JobEventMessage jobEventMessage);

    //@Mapping(target = "id", ignore = true)
    @Mapping(target = "jobId", source = "jobId")
    @Mapping(target = "eventType", source = "jobEventType", qualifiedByName = "mapJobEventTypeToString")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "eventData", source = "jobEventType", qualifiedByName = "extractRelevantEventData")
    JobEvent toJobEvent(JobEventMessage jobEventMessage);

   // @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobId", ignore = true)
    @Mapping(target = "status", source = "jobEventType", qualifiedByName = "mapStatus")
    @Mapping(target = "stepsDone", expression = "java((jobEventMessage.getJobEventType() instanceof korn03.tradeguardserver.kafka.events.jobUpdates.JobEventType.StepDone stepDone) ? stepDone.stepIndex() : job.getStepsDone())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", source = "timestamp")
    @Mapping(target = "userId", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExistingJob(@MappingTarget Job job, JobEventMessage jobEventMessage);

    // Kafka DTO
    @Mapping(target = "strategy", source = "name")
    @Mapping(target = "stepsTotal", source = "totalSteps")
    @Mapping(target = "durationMinutes", source = "durationMinutes")
    @Mapping(target = "coins", source = "coins")
    @Mapping(target = "side", source = "side")
    @Mapping(target = "force", expression = "java(calculateForce(dto.getDiscountPct()))")
    @Mapping(target = "discountPct", expression = "java(dto.getDiscountPct() != null ? dto.getDiscountPct() : 0.0)")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    @Mapping(target = "randomnessPct", expression = "java(0.0)")
    JobSubmissionKafkaDTO toKafkaDTO(JobSubmissionDTO dto);

    // Handle DCA-specific fields
    @Mapping(target = "totalAmt", source = "amount")
    @InheritConfiguration(name = "toKafkaDTO")
    JobSubmissionKafkaDTO toKafkaDTO(DcaJobSubmissionDTO dto);

    // Handle LIQ-specific fields
    @Mapping(target = "excludeSymbols", expression = "java(new java.util.ArrayList<>())")
    @Mapping(target = "proportionPct", source = "amount")
    @InheritConfiguration(name = "toKafkaDTO")
    JobSubmissionKafkaDTO toKafkaDTO(LiqJobSubmissionDTO dto);

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

    @Named("mapJobEventTypeToString")
    default String mapJobEventTypeToString(JobEventType eventType) {
        if (eventType instanceof JobEventType.Created) return "Created";
        if (eventType instanceof JobEventType.StepDone) return "StepDone";
        if (eventType instanceof JobEventType.OrdersPlaced) return "OrdersPlaced";
        if (eventType instanceof JobEventType.Paused) return "Paused";
        if (eventType instanceof JobEventType.Resumed) return "Resumed";
        if (eventType instanceof JobEventType.Stopped) return "Stopped";
        if (eventType instanceof JobEventType.Finished) return "Finished";
        if (eventType instanceof JobEventType.CanceledOrders) return "CanceledOrders";
        if (eventType instanceof JobEventType.ErrorEvent) return "Error";
        return "Unknown";
    }

    @Named("extractRelevantEventData")
    default String extractRelevantEventData(JobEventType eventType) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (eventType instanceof JobEventType.OrdersPlaced ordersPlaced) {
                return mapper.writeValueAsString(ordersPlaced.orders());
            }
            if (eventType instanceof JobEventType.StepDone stepDone) {
                return mapper.writeValueAsString(stepDone.stepIndex());
            }
            return null; // Skip data for other types
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event data", e);
        }
    }

    default Boolean calculateForce(Double discountPct) {
        return discountPct != null && discountPct > 0;
    }
    default String normalizeStrategy(String strategy) {
        return strategy != null ? strategy.toLowerCase() : null;
    }

    //@Mapping(target = "id", source = "id")
    @Mapping(target = "id", source = "jobId")
    @Mapping(target = "strategy", source = "strategy")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "stepsDone", source = "stepsDone")
    JobDTO toDTO(Job job);

    List<JobDTO> toDTOList(List<Job> jobs);

}
