package korn03.tradeguardserver.service.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class BybitAccountService {

    private final BybitApiAsyncAccountRestClient accountClient;

    public BybitAccountService(BybitApiAsyncAccountRestClient accountClient) {
        this.accountClient = accountClient;
    }

    /**
     * Fetch account balance for USDT (Unified Account)
     */
    public CompletableFuture<Map<String, Object>> fetchAccountBalance() {
        AccountDataRequest request = AccountDataRequest.builder()
                .accountType(com.bybit.api.client.domain.account.AccountType.UNIFIED)
                .category(CategoryType.LINEAR)
                .coin("USDT")
                .build();

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        accountClient.getWalletBalance(request, response -> future.complete((Map<String, Object>) response));
        return future;
    }
}
