package korn03.tradeguardserver.client;

import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncPositionRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import korn03.tradeguardserver.model.entity.user.UserBybitAccount;
import korn03.tradeguardserver.service.user.UserBybitAccountService;
import org.springframework.stereotype.Component;

@Component
public class BybitApiClientFactory {

    private final UserBybitAccountService userBybitAccountService;

    public BybitApiClientFactory(UserBybitAccountService userBybitAccountService) {
        this.userBybitAccountService = userBybitAccountService;
    }

    /**
     * Retrieves user-specific read-only API credentials and initializes a Bybit client.
     */
    private com.bybit.api.client.service.BybitApiClientFactory getReadOnlyClientFactory(Long userId, Long id) {
        UserBybitAccount account = userBybitAccountService.getBybitAccount(userId, id)
                .orElseThrow(() -> new RuntimeException("Bybit account not found"));

        String apiKey = userBybitAccountService.getDecryptedReadOnlyApiKey(account);
        String apiSecret = userBybitAccountService.getDecryptedReadOnlyApiSecret(account);

        return com.bybit.api.client.service.BybitApiClientFactory.newInstance(apiKey, apiSecret);
    }

    /**
     * Retrieves user-specific read-write API credentials and initializes a Bybit client.
     */
    private com.bybit.api.client.service.BybitApiClientFactory getReadWriteClientFactory(Long userId, Long id) {
        UserBybitAccount account = userBybitAccountService.getBybitAccount(userId, id)
                .orElseThrow(() -> new RuntimeException("Bybit account not found"));

        String apiKey = userBybitAccountService.getDecryptedReadWriteApiKey(account);
        String apiSecret = userBybitAccountService.getDecryptedReadWriteApiSecret(account);

        return com.bybit.api.client.service.BybitApiClientFactory.newInstance(apiKey, apiSecret);
    }

    /**
     * Returns a user-specific Bybit Account client (read-only).
     */
    public BybitApiAsyncAccountRestClient getReadOnlyAccountClient(Long userId, Long id) {
        return getReadOnlyClientFactory(userId, id).newAsyncAccountRestClient();
    }

    /**
     * Returns a user-specific Bybit Account client (read-write).
     */
    public BybitApiAsyncAccountRestClient getReadWriteAccountClient(Long userId, Long id) {
        return getReadWriteClientFactory(userId, id).newAsyncAccountRestClient();
    }

    /**
     * Returns a user-specific Bybit Trade client (read-write only).
     */
    public BybitApiAsyncTradeRestClient getTradeClient(Long userId, Long id) {
        return getReadWriteClientFactory(userId, id).newAsyncTradeRestClient();
    }

    /**
     * Returns a user-specific Bybit Position client (read-write only).
     */
    public BybitApiAsyncPositionRestClient getPositionClient(Long userId, Long id) {
        return getReadWriteClientFactory(userId, id).newAsyncPositionRestClient();
    }
}
