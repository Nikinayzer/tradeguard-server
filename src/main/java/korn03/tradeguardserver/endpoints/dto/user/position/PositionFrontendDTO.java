package korn03.tradeguardserver.endpoints.dto.user.position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionFrontendDTO {
    private String venue;
    private String symbol;
    private String side;
    private PositionSize size;
    private PositionPrices prices;
    private PositionPnl pnl;
    private BigDecimal leverage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionSize {
        private BigDecimal quantity;
        private BigDecimal value;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionPrices {
        private BigDecimal entry;
        private BigDecimal mark;
        private BigDecimal liquidation;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionPnl {
        private BigDecimal unrealized;
        private BigDecimal current;
        private BigDecimal cumulative;
    }
} 