package korn03.tradeguardserver.endpoints.dto.user.risk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskPatternDTO {
    @JsonProperty("pattern_id")
    private String patternId;
    
    @JsonProperty("job_id")
    private List<Long> jobIds;
    
    private List<String> coins;
    
    @JsonProperty("position_id")
    private String positionId;
    
    private String message;
    
    @JsonProperty("category_weights")
    private Map<String, Double> categoryWeights;
    
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
    
    private Double severity;
    
    private Boolean consumed;
    
    @JsonProperty("internal_id")
    private String internalId;
} 