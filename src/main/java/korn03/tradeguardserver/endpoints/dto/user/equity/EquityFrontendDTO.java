package korn03.tradeguardserver.endpoints.dto.user.equity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquityFrontendDTO {
    private String venue;
    private EquityBalances balances;
    private BigDecimal totalUnrealizedPnl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquityBalances {
        private BigDecimal wallet;
        private BigDecimal available;
        private BigDecimal bnb;
    }
} 