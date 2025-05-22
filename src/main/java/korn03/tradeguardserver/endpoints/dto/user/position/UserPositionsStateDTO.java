package korn03.tradeguardserver.endpoints.dto.user.position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPositionsStateDTO {
    private PositionSummary summary;
    private List<PositionFrontendDTO> activePositions;
    private List<PositionFrontendDTO> inactivePositions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionSummary {
        private BigDecimal totalPositionValue;
        private BigDecimal totalUnrealizedPnl;
        private int totalPositionsCount;
        private int activePositionsCount;
        private Instant lastUpdate;
    }
} 