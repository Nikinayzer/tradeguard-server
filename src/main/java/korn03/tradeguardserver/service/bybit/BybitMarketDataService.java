package korn03.tradeguardserver.service.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import korn03.tradeguardserver.endpoints.dto.InstrumentInfoDTO;
import korn03.tradeguardserver.endpoints.dto.MarketDataDTO;
import korn03.tradeguardserver.service.core.CacheService;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class BybitMarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(BybitMarketDataService.class);
    private static final String MARKET_KEY_PREFIX = "market_data:";
    private static final Duration CACHE_EXPIRATION = Duration.ofSeconds(60);
    private static final String COUNTER_CURRENCY = "USDT";
    private static final Map<String, List<String>> CRYPTO_CATEGORIES = Map.of(
            "Major Cryptocurrencies", List.of("BTC", "ETH", "BNB", "XRP", "SOL"),
            "New Tokens", List.of("ENA", "AIXBT", "FARTCOIN"),
            "Favorites", List.of("XLM", "HBAR", "LINK", "TRX", "TON", "WLD"),
            "Platform Tokens", List.of("ADA", "DOT", "AVAX", "ATOM", "NEAR"),
            "DeFi Tokens", List.of("UNI", "AAVE", "SUSHI", "COMP", "MKR"),
            "Privacy Coins", List.of("XMR", "ZEC", "DASH", "GRIN", "BEAM"),
            "Memecoins", List.of("DOGE", "1000PEPE", "TRUMP", "WIF", "MOODENG"),
            "Layer 2 Solutions", List.of("MATIC", "OP", "ARB", "IMX", "LRC"),
            "Emerging Tokens", List.of("APT", "SUI", "KAS", "RENDER", "GRT")
    );
    private final BybitApiMarketRestClient marketDataClient;
    private final CacheService cacheService;
    private final ExecutorService executorService;

    public BybitMarketDataService(BybitApiMarketRestClient marketDataClient, CacheService cacheService) {
        this.marketDataClient = marketDataClient;
        this.cacheService = cacheService;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * Fetch market data for all supported cryptocurrencies.
     *
     * @return Map of category to list of market data
     */
    //todo scheduled
    public Map<String, List<MarketDataDTO>> fetchAllMarketData() {
        Map<String, List<MarketDataDTO>> result = new HashMap<>();

        for (Map.Entry<String, List<String>> category : CRYPTO_CATEGORIES.entrySet()) {
            List<MarketDataDTO> categoryData = fetchMarketDataForCoins(category.getValue());
            if (!categoryData.isEmpty()) {
                result.put(category.getKey(), categoryData);
            }
        }

        return result;
    }

    /**
     * Fetch market data for a list of coins in parallel.
     *
     * @param coins List of coin symbols
     * @return List of MarketDataDTO
     */
    public List<MarketDataDTO> fetchMarketDataForCoins(List<String> coins) {
        List<MarketDataDTO> result = new ArrayList<>();
        List<String> coinsToFetch = new ArrayList<>();

        for (String coin : coins) {
            String cacheKey = MARKET_KEY_PREFIX + coin + COUNTER_CURRENCY;
            MarketDataDTO cachedData = cacheService.getFromCache(cacheKey, MarketDataDTO.class);
            if (cachedData != null) {
                result.add(cachedData);
            } else {
                coinsToFetch.add(coin);
            }
        }

        if (!coinsToFetch.isEmpty()) {
            List<CompletableFuture<MarketDataDTO>> futures = coinsToFetch.stream()
                    .map(coin -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return fetchMarketData(new CurrencyPair(coin, COUNTER_CURRENCY));
                        } catch (Exception e) {
                            logger.warn("Failed to fetch market data for {}: {}", coin, e.getMessage());
                            return null;
                        }
                    }, executorService))
                    .toList();

            List<MarketDataDTO> fetchedData = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();

            result.addAll(fetchedData);
        }

        return result;
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
            Object response = marketDataClient.getMarketTickers(request);
            Map<String, Object> marketResponse = (Map<String, Object>) response;

            if (marketResponse.isEmpty()) {
                throw new RuntimeException("Invalid market data response from Bybit API for " + pair);
            }

            MarketDataDTO marketData = parseMarketDataResponse(pair, marketResponse);

            InstrumentInfoDTO instrumentInfo = fetchInstrumentInfo(pair);
            marketData.setInstrumentInfo(instrumentInfo);

            cacheService.storeInCache(cacheKey, marketData, CACHE_EXPIRATION);

            logger.info("Fetched market data for {} : {}", pair, marketData);
            return marketData;
        } catch (Exception e) {
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

    /**
     * Fetch market data for specific tokens.
     *
     * @param tokens List of token symbols
     * @return List of market data for requested tokens
     */
    public List<MarketDataDTO> fetchSpecificTokens(List<String> tokens) {
        return fetchMarketDataForCoins(tokens);
    }

    /**
     * Fetch market data for a specific category.
     *
     * @param category Category name
     * @return List of market data for the category, empty list if category not found
     */
    public List<MarketDataDTO> fetchCategoryData(String category) {
        List<String> tokens = CRYPTO_CATEGORIES.get(category);
        if (tokens == null) {
            logger.warn("Category not found: {}", category);
            return Collections.emptyList();
        }
        return fetchMarketDataForCoins(tokens);
    }

    /**
     * Get all categories with their tokens.
     *
     * @return Map of category names to their token lists
     */
    public Map<String, List<String>> getAvailableCategories() {
        return CRYPTO_CATEGORIES;
    }

    /**
     * Get all available token symbols.
     *
     * @return Set of token symbols
     */
    public Set<String> getAllAvailableTokens() {
        return CRYPTO_CATEGORIES.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

}
