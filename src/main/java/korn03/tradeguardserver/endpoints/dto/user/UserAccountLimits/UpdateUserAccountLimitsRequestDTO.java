package korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserAccountLimitsRequestDTO {
    private String maxSingleJobLimit;
    private String maxDailyTradingLimit;
    private String maxConcurrentOrders;
    private String maxDailyTrades;
    private String tradingCooldown;
    private String maxPortfolioRisk;

//    private String minDcaDiscount;
//    private String minDcaSteps;
    private Boolean allowDcaForce;

//    private String minLiqTimeframe;
//    private String maxLiqProportion;
    private Boolean allowLiqForce;

    private String dailyLossLimit;
    private String maxConsecutiveLosses;
    private String maxDailyBalanceChange;
    private String volatilityLimit;
    private String liquidityThreshold;
}
