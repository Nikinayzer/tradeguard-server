package korn03.tradeguardserver.endpoints.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountLimitsDTO {
    private Long id;
    private Long userId;
    private BigDecimal dailyTradingLimit;
    private BigDecimal maximumLeverage;
    private Duration tradingCooldown;
    private BigDecimal dailyLossLimit;
} 