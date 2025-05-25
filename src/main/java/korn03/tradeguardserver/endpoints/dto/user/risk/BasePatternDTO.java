package korn03.tradeguardserver.endpoints.dto.user.risk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BasePatternDTO {
    @JsonProperty("pattern_id")
    private String patternId;
    
    @JsonProperty("job_id")
    private List<Long> jobIds;
    
    @JsonProperty("positions")
    private List<String> positions;
    
    private String message;
    
    @JsonProperty("category_weights")
    private Map<RiskCategory, Double> categoryWeights;
    
    private Map<String, Object> details;
    
    @JsonProperty("start_time")
    private Instant startTime;
    
    @JsonProperty("end_time")
    private Instant endTime;
    
    @JsonProperty("show_if_not_consumed")
    private Boolean showIfNotConsumed;
    
    @JsonProperty("is_composite")
    private Boolean isComposite;
    
    private Boolean unique;
    
    @JsonProperty("ttl_minutes")
    private Integer ttlMinutes;
    
    @JsonProperty("internal_id")
    private String internalId;
} 