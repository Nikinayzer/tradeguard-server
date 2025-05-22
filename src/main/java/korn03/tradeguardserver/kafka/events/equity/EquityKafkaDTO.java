package korn03.tradeguardserver.kafka.events.equity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquityKafkaDTO {
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("account_name")
    private String accountName;
    
    @JsonProperty("venue")
    private String venue;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("equity")
    private EquityDetailsKafkaDTO equity;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EquityDetailsKafkaDTO {
        @JsonProperty("wallet_balance")
        private BigDecimal walletBalance;
        
        @JsonProperty("available_balance")
        private BigDecimal availableBalance;
        
        @JsonProperty("total_unrealized_pnl")
        private BigDecimal totalUnrealizedPnl;
        
        @JsonProperty("bnb_balance_usdt")
        private BigDecimal bnbBalanceUsdt;
    }
} 