package korn03.tradeguardserver.service.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import korn03.tradeguardserver.endpoints.dto.InstrumentInfoDTO;
import korn03.tradeguardserver.endpoints.dto.MarketDataDTO;
import korn03.tradeguardserver.service.CacheService;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class BybitMarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(BybitMarketDataService.class);

    private static final String MARKET_KEY_PREFIX = "market_data:";
    private static final Duration CACHE_EXPIRATION = Duration.ofSeconds(60);
    private final BybitApiMarketRestClient marketDataClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheService cacheService;

    public BybitMarketDataService(BybitApiMarketRestClient marketDataClient, RedisTemplate<String, Object> redisTemplate, CacheService cacheService) {
        this.marketDataClient = marketDataClient;
        this.redisTemplate = redisTemplate;
        this.cacheService = cacheService;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void runOnStartup() {
        fetchMarketData(CurrencyPair.BTC_USDT);
    }

    /**
     * Fetch market data for a specific pair and append instrument info.
     *
     * @return MarketDataDTO
     */
    public MarketDataDTO fetchMarketData(CurrencyPair pair) {
        String cacheKey = "market_data:" + pair.base.getSymbol() + pair.counter.getSymbol();

        MarketDataDTO cachedData = cacheService.getFromCache(cacheKey, MarketDataDTO.class);
        if (cachedData != null) {
            return cachedData;
        }

        MarketDataRequest request = MarketDataRequest.builder().category(CategoryType.LINEAR).symbol(pair.base.getSymbol() + pair.counter.getSymbol()).build();

        try {
            // Fetch market data
            Object response = marketDataClient.getMarketTickers(request);
            Map<String, Object> marketResponse = (Map<String, Object>) response;

            if (marketResponse.isEmpty()) {
                throw new RuntimeException("Invalid market data response from Bybit API for " + pair);
            }

            MarketDataDTO marketData = parseMarketDataResponse(pair, marketResponse);

            // Fetch Instrument Info
            InstrumentInfoDTO instrumentInfo = fetchInstrumentInfo(pair);
            marketData.setInstrumentInfo(instrumentInfo);

            cacheService.storeInCache(cacheKey, marketData,CACHE_EXPIRATION);

            logger.info("Fetched market data for {} : {}", pair, marketData);
            return marketData;
        } catch (Exception e) {
            logger.error("Failed to fetch market data for {}: {}", pair, e.getMessage());
            throw new RuntimeException("Failed to fetch market data for " + pair, e);
        }
    }


    /**
     * Fetches instrument info from Bybit and returns an InstrumentInfoDTO.
     */
    private InstrumentInfoDTO fetchInstrumentInfo(CurrencyPair pair) {
        MarketDataRequest request = MarketDataRequest.builder().category(CategoryType.LINEAR).symbol(pair.base.getSymbol() + pair.counter.getSymbol()).build();

        try {
            Map<String, Object> response = (Map<String, Object>) marketDataClient.getInstrumentsInfo(request);
            return parseInstrumentInfo(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch instrument info for " + pair, e);
        }
    }

    /**
     * Parses Bybit's response into InstrumentInfoDTO.
     */
    private InstrumentInfoDTO parseInstrumentInfo(Map<String, Object> response) {

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("No instrument data available");
        }

        Map<String, Object> data = list.get(0);

        InstrumentInfoDTO instrumentInfo = new InstrumentInfoDTO();
        instrumentInfo.setPriceScale(Integer.parseInt((String) data.get("priceScale")));
        instrumentInfo.setQuantityStep(new BigDecimal((String) ((Map<?, ?>) data.get("lotSizeFilter")).get("qtyStep")));
        instrumentInfo.setTimestamp(System.currentTimeMillis());

        return instrumentInfo;
    }

    /**
     * Parses Bybit's response into MarketDataDTO.
     */
    private MarketDataDTO parseMarketDataResponse(CurrencyPair pair, Map<String, Object> response) {
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get("list");

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("No market data available for " + pair);
        }

        Map<String, Object> data = list.get(0);

        MarketDataDTO marketData = new MarketDataDTO();
        marketData.setInstrument(pair);
        marketData.setCurrentPrice(new BigDecimal((String) data.get("lastPrice")));
        marketData.setHigh24h(new BigDecimal((String) data.get("highPrice24h")));
        marketData.setPrice24hAgo(new BigDecimal((String) data.get("prevPrice24h")));
        marketData.setPrice1hAgo(new BigDecimal((String) data.get("prevPrice1h")));
        marketData.setLow24h(new BigDecimal((String) data.get("lowPrice24h")));
        marketData.setVolume24h(new BigDecimal((String) data.get("volume24h")));
        marketData.setChange24h(new BigDecimal((String) data.get("price24hPcnt")));
        marketData.setOpenInterestValue(new BigDecimal((String) data.get("openInterestValue")));
        marketData.setFundingRate(new BigDecimal((String) data.get("fundingRate")));
        marketData.setNextFundingTime(Long.parseLong((String) data.get("nextFundingTime")));

        return marketData;
    }

}
