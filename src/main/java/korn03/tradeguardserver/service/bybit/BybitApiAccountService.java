package korn03.tradeguardserver.service.bybit;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import korn03.tradeguardserver.client.BybitApiClientManager;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class BybitApiAccountService {

    private final BybitApiClientManager apiClientManager;

    public BybitApiAccountService(BybitApiClientManager apiClientManager) {
        this.apiClientManager = apiClientManager;
    }

    /**
     * Fetch user-specific account balance for USDT.
     */
    public CompletableFuture<Map<String, Object>> fetchAccountBalance(Long userId, String accountName) {
        // Get the correct API client for the user
        BybitApiAsyncAccountRestClient accountClient = apiClientManager.getAccountClient(userId, accountName);

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
