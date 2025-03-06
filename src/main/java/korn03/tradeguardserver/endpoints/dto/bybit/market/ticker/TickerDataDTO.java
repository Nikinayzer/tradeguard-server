package korn03.tradeguardserver.endpoints.dto.bybit.market.ticker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerDataDTO {
    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal indexPrice;
    private BigDecimal markPrice;

    @JsonProperty("prevPrice24h")
    private BigDecimal prevPrice24h;

    @JsonProperty("price24hPcnt")
    private BigDecimal price24hPcnt;

    private BigDecimal highPrice24h;
    private BigDecimal lowPrice24h;

    @JsonProperty("prevPrice1h")
    private BigDecimal prevPrice1h;

    @JsonProperty("openInterest")
    private BigDecimal openInterest;

    private BigDecimal openInterestValue;

    @JsonProperty("turnover24h")
    private BigDecimal turnover24h;

    private BigDecimal volume24h;
    private BigDecimal fundingRate;
    private Long nextFundingTime;

    @JsonProperty("predictedDeliveryPrice")
    private String predictedDeliveryPrice;

    @JsonProperty("basisRate")
    private String basisRate;

    @JsonProperty("deliveryFeeRate")
    private String deliveryFeeRate;

    @JsonProperty("deliveryTime")
    private Long deliveryTime;

    @JsonProperty("ask1Size")
    private BigDecimal ask1Size;

    @JsonProperty("bid1Price")
    private BigDecimal bid1Price;

    @JsonProperty("ask1Price")
    private BigDecimal ask1Price;

    @JsonProperty("bid1Size")
    private BigDecimal bid1Size;

    @JsonProperty("basis")
    private String basis;

    @JsonProperty("preOpenPrice")
    private String preOpenPrice;

    @JsonProperty("preQty")
    private String preQty;

    @JsonProperty("curPreListingPhase")
    private String curPreListingPhase;
}

