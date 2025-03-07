package korn03.tradeguardserver.endpoints.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountLimitsRequestDTO {
    private BigDecimal dailyTradingLimit;
    private BigDecimal maximumLeverage;
    private Duration tradingCooldown;
    private BigDecimal dailyLossLimit;
} 