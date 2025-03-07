package korn03.tradeguardserver.endpoints.controller.market;

import korn03.tradeguardserver.endpoints.dto.MarketDataDTO;
import korn03.tradeguardserver.service.bybit.BybitApiMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/market")
public class MarketDataController {

    private final BybitApiMarketDataService marketDataService;

    public MarketDataController(BybitApiMarketDataService marketDataService) {
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
}
