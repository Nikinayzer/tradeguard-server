package korn03.tradeguardserver.kafka.events.equity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a user's account equity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Equity {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("venue")
    private String venue;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("wallet_balance")
    private BigDecimal walletBalance;

    @JsonProperty("available_balance")
    private BigDecimal availableBalance;

    @JsonProperty("total_unrealized_pnl")
    private BigDecimal totalUnrealizedPnl;

    @JsonProperty("bnb_balance_usdt")
    private BigDecimal bnbBalanceUsdt;

    @Transient
    @JsonIgnore
    private String equityKey;

    public String getEquityKey() {
        return  venue;
    }
}