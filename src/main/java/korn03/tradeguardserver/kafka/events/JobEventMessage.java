package korn03.tradeguardserver.kafka.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

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

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("event_type")
    private JobEventType jobEventType;

    @JsonProperty("steps_done")
    private Integer stepsDone;

    @JsonProperty("name")
    private String name;

    @JsonProperty("coins")
    private List<String> coins;

    @JsonProperty("side")
    private String side;

    @JsonProperty("discount_pct")
    private Double discountPct;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("steps_total")
    private Integer stepsTotal;

    @JsonProperty("duration_minutes")
    private Double durationMinutes;

    @JsonProperty("timestamp")
    private Instant timestamp;
}
