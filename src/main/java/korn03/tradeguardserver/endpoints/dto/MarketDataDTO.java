package korn03.tradeguardserver.endpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knowm.xchange.currency.CurrencyPair;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketDataDTO {
    private CurrencyPair instrument;
    private BigDecimal currentPrice;
    private BigDecimal change24h;
    private BigDecimal high24h;
    private BigDecimal low24h;
    private BigDecimal price24hAgo;
    private BigDecimal price1hAgo;
    private BigDecimal volume24h;
    private BigDecimal openInterestValue;
    private BigDecimal fundingRate;
    private Long nextFundingTime;
    private InstrumentInfoDTO instrumentInfo;
}
