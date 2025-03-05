package korn03.tradeguardserver.endpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderDTO {
    private String orderId;
    private String orderLinkId;
    private String blockTradeId;
    private String symbol;
    private String price;
    private String qty;
    private String side;
}
