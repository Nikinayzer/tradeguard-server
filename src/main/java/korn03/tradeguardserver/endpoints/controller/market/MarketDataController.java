package korn03.tradeguardserver.endpoints.controller.market;

import korn03.tradeguardserver.endpoints.dto.MarketDataDTO;
import korn03.tradeguardserver.service.bybit.BybitMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/market")
public class MarketDataController {

    private final BybitMarketDataService marketDataService;

    public MarketDataController(BybitMarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    /**
     * Get market data for a specific trading pair.
     * Example: GET /api/market?base=BTC&counter=USDT
     */
    @GetMapping
    public ResponseEntity<MarketDataDTO> getMarketData(
            @RequestParam String base,
            @RequestParam String counter) {
        try {
            CurrencyPair pair = new CurrencyPair(base, counter);

            // Fetch market data
            MarketDataDTO marketData = marketDataService.fetchMarketData(pair);

            return ResponseEntity.ok(marketData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Get market data for all supported cryptocurrencies, categorized by type.
     * Example: GET /api/market/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, List<MarketDataDTO>>> getAllMarketData() {
        try {
            Map<String, List<MarketDataDTO>> allMarketData = marketDataService.fetchAllMarketData();
            return ResponseEntity.ok(allMarketData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Get market data for specific tokens.
     * Example: GET /api/market/tokens?symbols=BTC,ETH,SOL
     */
    @GetMapping("/tokens")
    public ResponseEntity<List<MarketDataDTO>> getSpecificTokens(@RequestParam List<String> symbols) {
        try {
            List<MarketDataDTO> tokenData = marketDataService.fetchSpecificTokens(symbols);
            return ResponseEntity.ok(tokenData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    /**
     * Get all categories and their associated tokens.
     * Example: GET /api/market/categories
     * Returns a map where keys are category names and values are lists of token symbols
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, List<String>>> getAvailableCategories() {
        return ResponseEntity.ok(marketDataService.getAvailableCategories());
    }

    /**
     * Get all available token symbols.
     * Example: GET /api/market/available-tokens
     */
    @GetMapping("/available-tokens")
    public ResponseEntity<Set<String>> getAvailableTokens() {
        return ResponseEntity.ok(marketDataService.getAllAvailableTokens());
    }
}
