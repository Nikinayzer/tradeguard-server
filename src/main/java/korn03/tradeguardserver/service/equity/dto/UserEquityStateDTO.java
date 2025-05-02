package korn03.tradeguardserver.service.equity.dto;

import korn03.tradeguardserver.kafka.events.equity.Equity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object representing a user's combined equity state across all venues.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEquityStateDTO {
    
    /**
     * The user ID
     */
    private Long userId;
    
    /**
     * Total wallet balance across all venues (in USDT)
     */
    private BigDecimal totalWalletBalance;
    
    /**
     * Total available balance across all venues (in USDT)
     */
    private BigDecimal totalAvailableBalance;
    
    /**
     * Total unrealized PnL across all venues (in USDT)
     */
    private BigDecimal totalUnrealizedPnl;
    
    /**
     * Total BNB balance value across all venues (in USDT)
     */
    private BigDecimal totalBnbBalanceUsdt;
    
    /**
     * The timestamp of this equity state snapshot
     */
    private Instant timestamp;
    
    /**
     * Detailed equity information for each venue
     */
    private List<Equity> venueEquities;
} 