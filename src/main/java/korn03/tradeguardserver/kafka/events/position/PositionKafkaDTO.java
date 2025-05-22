package korn03.tradeguardserver.kafka.events.position;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionKafkaDTO {
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("venue")
    private String venue;
    
    @JsonProperty("account_name")
    private String accountName;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("position")
    private PositionDetailsKafkaDTO position;
    
    @JsonProperty("update_type")
    @JsonDeserialize(using = PositionUpdateTypeDeserializer.class)
    private PositionUpdateType updateType;
    
    @Transient
    @JsonIgnore
    private String positionKey;
    
    public String getPositionKey() {
        return venue + "_" + position.getSymbol();
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PositionDetailsKafkaDTO {
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
    }
} 