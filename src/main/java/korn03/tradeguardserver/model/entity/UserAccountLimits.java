package korn03.tradeguardserver.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;

@Entity
@Table(name = "user_account_limits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountLimits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "daily_trading_limit", nullable = false)
    private BigDecimal dailyTradingLimit;

    @Column(name = "maximum_leverage", nullable = false)
    private BigDecimal maximumLeverage;

    @Column(name = "trading_cooldown", nullable = false)
    private Duration tradingCooldown;

    @Column(name = "daily_loss_limit", nullable = false)
    private BigDecimal dailyLossLimit;
} 