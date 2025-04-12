package korn03.tradeguardserver.kafka.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a job event message sent/received via Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobEventMessage {

    @JsonProperty("job_id")
    private Long jobId;

    @JsonProperty("event_type")
    @JsonDeserialize(using = JobEventTypeDeserializer.class)
    @JsonSerialize(using = JobEventTypeSerializer.class)
    private JobEventType jobEventType;

    @JsonProperty("source")
    private String source;

    @JsonProperty("timestamp")
    private Instant timestamp;
}
