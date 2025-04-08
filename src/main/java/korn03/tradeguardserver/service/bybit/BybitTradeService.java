//package korn03.tradeguardserver.service.exchange;
//
//import com.exchange.api.client.domain.CategoryType;
//import com.exchange.api.client.domain.TradeOrderType;
//import com.exchange.api.client.domain.trade.Side;
//import com.exchange.api.client.domain.trade.TimeInForce;
//import com.exchange.api.client.domain.trade.request.TradeOrderRequest;
//import com.exchange.api.client.restApi.BybitApiAsyncTradeRestClient;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//public class BybitTradeService {
//
//    private final BybitApiAsyncTradeRestClient tradeClient;
//
//    public BybitTradeService(BybitApiAsyncTradeRestClient tradeClient) {
//        this.tradeClient = tradeClient;
//    }
//
//    /**
//     * Place a market order.
//     */
//    public CompletableFuture<Map<String, Object>> placeMarketOrder(String symbol, Side side, String quantity) {
//        TradeOrderRequest orderRequest = TradeOrderRequest.builder()
//                .category(CategoryType.LINEAR)
//                .symbol(symbol)
//                .side(side)
//                .orderType(TradeOrderType.MARKET)
//                .qty(quantity)
//                .timeInForce(TimeInForce.GOOD_TILL_CANCEL)
//                .build();
//
//        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
//        tradeClient.createOrder(orderRequest, response -> future.complete((Map<String, Object>) response));
//        return future;
//    }
//
//    /**
//     * Place a limit order.
//     */
//    public CompletableFuture<Map<String, Object>> placeLimitOrder(String symbol, Side side, String quantity, String price) {
//        TradeOrderRequest orderRequest = TradeOrderRequest.builder()
//                .category(CategoryType.LINEAR)
//                .symbol(symbol)
//                .side(side)
//                .orderType(TradeOrderType.LIMIT)
//                .qty(quantity)
//                .price(price)
//                .timeInForce(TimeInForce.GOOD_TILL_CANCEL)
//                .build();
//
//        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
//        tradeClient.createOrder(orderRequest, response -> future.complete((Map<String, Object>) response));
//        return future;
//    }
//
//    /**
//     * Cancel all orders for a specific symbol.
//     */
//    public CompletableFuture<Map<String, Object>> cancelAllOrders(String symbol) {
//        TradeOrderRequest cancelRequest = TradeOrderRequest.builder()
//                .category(CategoryType.LINEAR)
//                .symbol(symbol)
//                .build();
//
//        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
//        tradeClient.cancelAllOrder(cancelRequest, response -> future.complete((Map<String, Object>) response));
//        return future;
//    }
//}
