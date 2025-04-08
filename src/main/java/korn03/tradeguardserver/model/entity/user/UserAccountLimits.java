package korn03.tradeguardserver.model.entity.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /*
     * 📌 Pre-Trade Limits
     * Checked at creation of a job.
     */

    //todo add value for dca proportion
    @Column(name = "max_single_job_limit", nullable = false)
    private BigDecimal maxSingleJobLimit; //MAX SINGLE JOB AMOUNT. ABSOLUTE

    @Column(name = "max_daily_trading_limit", nullable = false)
    private BigDecimal maxDailyTradingLimit; // MAX DAILY TRADE AMOUNT. ABSOLUTE

    @Column(name = "max_portfolio_risk", nullable = false)
    private BigDecimal maxPortfolioRisk; // % OF ALL ASSETS IN ONE TRADE

    @Column(name = "max_concurrent_orders", nullable = false)
    private Integer maxConcurrentOrders; // MAX NUMBER OF ACTIVE JOBS

    @Column(name = "max_daily_trades", nullable = false)
    private Integer maxDailyTrades; // MAX AMOUNT OF TRADES DAILY

    @Column(name = "trading_cooldown", nullable = false)
    private Integer tradingCooldown; // TIME GAP BETWEEN JOBS

    @Column(name = "allow_force_dca", nullable = false)
    private Boolean allowDcaForce; // DISABLE OR ENABLE FORCE DCA

    @Column(name = "allow_force_liq", nullable = false)
    private Boolean allowLiqForce; // DISABLE OR ENABLE FORCE LIQIDATION

    /*
     * 🔄 Continuous Monitoring Limits
     *  Checked on OrderPlaced step.
     *  If violated, the job is PAUSED/CANCELED.
     */

    @Column(name = "daily_loss_limit", nullable = false)
    private BigDecimal dailyLossLimit; // Stops trading if user loses too much

    @Column(name = "max_consecutive_losses", nullable = false)
    private Integer maxConsecutiveLosses; // Detects loss-chasing/revenge trading

    @Column(name = "max_daily_balance_change", nullable = false)
    private BigDecimal maxDailyBalanceChange; // Stops trading if balance fluctuates too much

    @Column(name = "volatility_limit", nullable = false)
    private BigDecimal volatilityLimit; // Prevents trading in extreme market volaility

    @Column(name = "liquidity_threshold", nullable = false)
    private BigDecimal liquidityThreshold; // Stops trading in low liquidity conditions

}
