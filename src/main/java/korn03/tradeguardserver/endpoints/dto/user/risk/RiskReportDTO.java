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
public class RiskReportDTO {
    @JsonProperty("event_type")
    private String eventType = "RiskReport";
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("top_risk_level")
    private RiskLevel topRiskLevel;
    
    @JsonProperty("top_risk_confidence")
    private Double topRiskConfidence;
    
    @JsonProperty("top_risk_type")
    private RiskCategory topRiskType;
    
    @JsonProperty("category_scores")
    private Map<RiskCategory, Double> categoryScores;
    
    private List<AtomicPatternDTO> patterns;
    
    @JsonProperty("composite_patterns")
    private List<CompositePatternDTO> compositePatterns;
    
    private Instant timestamp;
    
    @JsonProperty("atomic_patterns_number")
    private Integer atomicPatternsNumber;
    
    @JsonProperty("composite_patterns_number")
    private Integer compositePatternsNumber;
    
    @JsonProperty("consumed_patterns_number")
    private Integer consumedPatternsNumber;
} 