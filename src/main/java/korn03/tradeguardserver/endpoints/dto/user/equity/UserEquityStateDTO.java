package korn03.tradeguardserver.endpoints.dto.user.equity;

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
public class UserEquityStateDTO {
    private EquitySummary summary;
    private List<EquityFrontendDTO> venueEquities;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquitySummary {
        private BigDecimal totalWalletBalance;
        private BigDecimal totalAvailableBalance;
        private BigDecimal totalUnrealizedPnl;
        private BigDecimal totalBnbBalance;
        private Instant lastUpdate;
    }
} 