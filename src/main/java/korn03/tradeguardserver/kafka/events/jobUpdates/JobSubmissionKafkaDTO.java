package korn03.tradeguardserver.kafka.events.jobUpdates;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class JobSubmissionKafkaDTO {
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("strategy")
    private String strategy;
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("steps_total")
    private Integer stepsTotal;
    
    @JsonProperty("duration_minutes")
    private Double durationMinutes;
    
    @JsonProperty("coins")
    private List<String> coins;
    
    @JsonProperty("side")
    private String side;

    @JsonProperty("force")
    private Boolean force;
    
    @JsonProperty("discount_pct")
    private Double discountPct;
    
    @JsonProperty("randomness_pct")
    private Double randomnessPct = 0.0;

    // LIQ-specific fields
    @JsonProperty("exclude_symbols")
    private List<String> excludeSymbols = List.of();
    
    @JsonProperty("proportion_pct")
    private Double proportionPct;

    // DCA-specific fields
    @JsonProperty("total_amt")
    private Double totalAmt;
}