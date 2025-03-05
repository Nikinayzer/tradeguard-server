package korn03.tradeguardserver.service.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class BybitMarketDataService {

    private final BybitApiMarketRestClient marketDataClient;

    public BybitMarketDataService(BybitApiMarketRestClient marketDataClient) {
        this.marketDataClient = marketDataClient;
    }

    /**
     * Fetch market data (tickers) for a specific trading pair.
     */
    public Map<String, Object> fetchMarketData(String symbol) {
        MarketDataRequest request = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(symbol)
                .build();

        return (Map<String, Object>) marketDataClient.getMarketTickers(request);
    }

    /**
     * Get the latest price for a given trading pair.
     */
    public BigDecimal getCurrentPrice(String symbol) {
        Map<String, Object> marketData = fetchMarketData(symbol);

        Map<?, ?> result = (Map<?, ?>) marketData.get("result");
        List<?> list = (List<?>) result.get("list");

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("No market data available for " + symbol);
        }

        Map<?, ?> firstEntry = (Map<?, ?>) list.get(0);
        String lastPrice = (String) firstEntry.get("lastPrice");

        return new BigDecimal(lastPrice);
    }

    /**
     * Get instrument details (precision, quantity steps, etc.)
     */
    public Map<String, Object> getInstrumentInfo(String symbol) {
        MarketDataRequest request = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(symbol)
                .build();

        return (Map<String, Object>) marketDataClient.getInstrumentsInfo(request);
    }

    /**
     * Extract key instrument info (price precision, quantity step).
     */
    public InstrumentInfo fetchInstrumentInfoFromApi(String symbol) {
        Map<String, Object> response = getInstrumentInfo(symbol);

        Map<?, ?> result = (Map<?, ?>) response.get("result");
        List<?> list = (List<?>) result.get("list");

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("No instrument data available for " + symbol);
        }

        Map<?, ?> firstEntry = (Map<?, ?>) list.get(0);
        int priceScale = Integer.parseInt((String) firstEntry.get("priceScale"));
        BigDecimal qtyStep = new BigDecimal((String) ((Map<?, ?>) firstEntry.get("lotSizeFilter")).get("qtyStep"));

        return new InstrumentInfo(priceScale, qtyStep);
    }

    /**
     * Data class to store instrument info.
     */
    public static class InstrumentInfo {
        private final int priceScale;
        private final BigDecimal qtyStep;

        public InstrumentInfo(int priceScale, BigDecimal qtyStep) {
            this.priceScale = priceScale;
            this.qtyStep = qtyStep;
        }

        public int getPriceScale() {
            return priceScale;
        }

        public BigDecimal getQtyStep() {
            return qtyStep;
        }
    }
}
