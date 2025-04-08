//package korn03.tradeguardserver.service.exchange;
//
//import com.exchange.api.client.domain.CategoryType;
//import com.exchange.api.client.domain.position.request.PositionDataRequest;
//import com.exchange.api.client.restApi.BybitApiAsyncPositionRestClient;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//public class BybitPositionService {
//
//    private final BybitApiAsyncPositionRestClient positionClient;
//
//    public BybitPositionService(BybitApiAsyncPositionRestClient positionClient) {
//        this.positionClient = positionClient;
//    }
//
//    /**
//     * Fetch all open positions for USDT Perpetual (Linear)
//     */
//    public CompletableFuture<List<Map<String, Object>>> fetchOpenPositions() {
//        PositionDataRequest request = PositionDataRequest.builder()
//                .category(CategoryType.LINEAR)
//                .settleCoin("USDT")
//                .build();
//
//        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
//        positionClient.getPositionInfo(request, response -> {
//            Map<String, Object> result = (Map<String, Object>) response;
//            List<Map<String, Object>> positions = (List<Map<String, Object>>) result.get("result");
//            future.complete(positions);
//        });
//
//        return future;
//    }
//
//    /**
//     * Set leverage for a given coin
//     */
//    public CompletableFuture<Map<String, Object>> setLeverage(String symbol, double leverage) {
//        PositionDataRequest request = PositionDataRequest.builder()
//                .category(CategoryType.LINEAR)
//                .symbol(symbol)
//                .buyLeverage(String.valueOf(leverage))
//                .sellLeverage(String.valueOf(leverage))
//                .build();
//
//        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
//        positionClient.setPositionLeverage(request, response -> future.complete((Map<String, Object>) response));
//
//        return future;
//    }
//}
