package korn03.tradeguardserver.kafka.events.position;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

enum PositionUpdateType {
    INCREASED, DECREASED, CLOSED, SNAPSHOT;
}

/**
 * Represents a trading position message sent/received via Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("venue")
    private String venue;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("side")
    private String side;

    @JsonProperty("qty")
    private BigDecimal qty;

    @JsonProperty("usdt_amt")
    private BigDecimal usdtAmt;

    @JsonProperty("entry_price")
    private BigDecimal entryPrice;

    @JsonProperty("mark_price")
    private BigDecimal markPrice;

    @JsonProperty("liquidation_price")
    private BigDecimal liquidationPrice;

    @JsonProperty("unrealized_pnl")
    private BigDecimal unrealizedPnl;

    @JsonProperty("cur_realized_pnl")
    private BigDecimal curRealizedPnl;

    @JsonProperty("cum_realized_pnl")
    private BigDecimal cumRealizedPnl;

    @JsonProperty("leverage")
    private BigDecimal leverage;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("update_type")
    @Enumerated(EnumType.STRING)
    private PositionUpdateType updateType;

    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Instant timestamp;

    @Transient
    private String positionKey;

    public String getPositionKey() {
        return userId + "_" + venue + "_" + symbol;
    }
}
