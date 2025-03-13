package korn03.tradeguardserver.kafka.events;

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

    private String name;
    private List<String> coins;
    private String side;

    @JsonProperty("discount_pct")
    private Double discountPct;

    private Double amount;

    @JsonProperty("steps_total")
    private Integer stepsTotal;

    @JsonProperty("duration_minutes")
    private Double durationMinutes;

    private Instant timestamp;

    public static JobEventMessage createNewJob(
            Long jobId,
            Long userId,
            String name,
            List<String> coins,
            String side,
            Double discountPct,
            Double amount,
            Integer stepsTotal,
            Double durationMinutes
    ) {
        return JobEventMessage.builder()
                .jobId(jobId)
                .userId(userId)
                .jobEventType(JobEventType.of("Created"))
                .stepsDone(0)
                .name(name)
                .coins(coins)
                .side(side)
                .discountPct(discountPct)
                .amount(amount)
                .stepsTotal(stepsTotal)
                .durationMinutes(durationMinutes)
                .timestamp(Instant.now())
                .build();
    }

    public static JobEventMessage createPausedJob(
            Long jobId,
            Integer stepsDone
    ) {
        return JobEventMessage.builder()
                .jobId(jobId)
                .jobEventType(JobEventType.of("Paused"))
                .stepsDone(stepsDone)
                .timestamp(Instant.now())
                .build();
    }

    public static JobEventMessage createResumedJob(
            Long jobId,
            Integer stepsDone
    ) {
        return JobEventMessage.builder()
                .jobId(jobId)
                .jobEventType(JobEventType.of("Resumed"))
                .stepsDone(stepsDone)
                .timestamp(Instant.now())
                .build();
    }

    public static JobEventMessage createCanceledOrdersJob(
            Long jobId,
            Integer stepsDone
    ) {
        return JobEventMessage.builder()
                .jobId(jobId)
                .jobEventType(JobEventType.of("CanceledOrders"))
                .stepsDone(stepsDone)
                .timestamp(Instant.now())
                .build();
    }
}
