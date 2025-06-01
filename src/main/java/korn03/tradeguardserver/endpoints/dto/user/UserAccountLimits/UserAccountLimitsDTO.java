package korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountLimitsDTO {
    private Long id;
    private Long userId;

    // Pre-Trade Validation Limits
    private BigDecimal maxSingleJobLimit;
    private BigDecimal maxDailyTradingLimit;
    private BigDecimal maxPortfolioRisk;
    private Integer maxConcurrentOrders;
    private Integer maxDailyTrades;
    private Integer tradingCooldown;

    private Boolean allowDcaForce;
    private Boolean allowLiqForce;

    // Continuous Monitoring Limits
    private BigDecimal dailyLossLimit;
    private Integer maxConsecutiveLosses;
    private BigDecimal maxDailyBalanceChange;
    private BigDecimal volatilityLimit;
    private BigDecimal liquidityThreshold;
}
