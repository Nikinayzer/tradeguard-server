package korn03.tradeguardserver.endpoints.dto;

import com.bybit.api.client.domain.trade.Side;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knowm.xchange.currency.CurrencyPair;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionInfoDTO {
    private CurrencyPair instrument;
    private BigDecimal qty;
    private BigDecimal usdValue;
    private Side side;
    private BigDecimal markPrice;
    private BigDecimal entryPrice;
    private BigDecimal leverage;
    private BigDecimal liquidationPrice;
    private BigDecimal unrealizedPnl;
    private BigDecimal cumRealizedPnl;
    private MarketDataDTO marketData;
}
