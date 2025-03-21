package korn03.tradeguardserver.mapper;

import korn03.tradeguardserver.endpoints.dto.user.job.DcaJobSubmissionDTO;
import korn03.tradeguardserver.endpoints.dto.user.job.LiqJobSubmissionDTO;
import korn03.tradeguardserver.kafka.events.DcaJobSubmissionMessage;
import korn03.tradeguardserver.kafka.events.LiqJobSubmissionMessage;
import korn03.tradeguardserver.mapper.util.MapStructConverters;
import org.mapstruct.*;

import java.time.Instant;

@Mapper(
    componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = MapStructConverters.class,
    imports = {
        Instant.class
    }
)
public interface JobSubmissionMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "timestamp", expression = "java(Instant.now().toEpochMilli())")
    DcaJobSubmissionMessage toDcaMessage(DcaJobSubmissionDTO dto, Long userId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "timestamp", expression = "java(Instant.now().toEpochMilli())")
    LiqJobSubmissionMessage toLiqMessage(LiqJobSubmissionDTO dto, Long userId);

    @AfterMapping
    default void setDefaults(@MappingTarget DcaJobSubmissionMessage message) {
        if (message.getForce() == null) {
            message.setForce(false);
        }
    }

    @AfterMapping
    default void setDefaults(@MappingTarget LiqJobSubmissionMessage message) {
        if (message.getForce() == null) {
            message.setForce(false);
        }
    }
} 