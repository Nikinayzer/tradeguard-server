package korn03.tradeguardserver.client;

import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncPositionRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import korn03.tradeguardserver.model.entity.UserBybitAccount;
import korn03.tradeguardserver.service.user.UserBybitAccountService;
import korn03.tradeguardserver.service.core.EncryptionService;
import org.springframework.stereotype.Component;

@Component
public class BybitApiClientManager {

    private final UserBybitAccountService userBybitAccountService;

    public BybitApiClientManager(UserBybitAccountService userBybitAccountService) {
        this.userBybitAccountService = userBybitAccountService;
    }

    /**
     * Retrieves user-specific read-only API credentials and initializes a Bybit client.
     */
    private BybitApiClientFactory getReadOnlyClientFactory(Long userId, String accountName) {
        UserBybitAccount account = userBybitAccountService.getBybitAccount(userId, accountName)
                .orElseThrow(() -> new RuntimeException("Bybit account not found"));

        String apiKey = userBybitAccountService.getDecryptedReadOnlyApiKey(account);
        String apiSecret = userBybitAccountService.getDecryptedReadOnlyApiSecret(account);

        return BybitApiClientFactory.newInstance(apiKey, apiSecret);
    }

    /**
     * Retrieves user-specific read-write API credentials and initializes a Bybit client.
     */
    private BybitApiClientFactory getReadWriteClientFactory(Long userId, String accountName) {
        UserBybitAccount account = userBybitAccountService.getBybitAccount(userId, accountName)
                .orElseThrow(() -> new RuntimeException("Bybit account not found"));

        String apiKey = userBybitAccountService.getDecryptedReadWriteApiKey(account);
        String apiSecret = userBybitAccountService.getDecryptedReadWriteApiSecret(account);

        return BybitApiClientFactory.newInstance(apiKey, apiSecret);
    }

    /**
     * Returns a user-specific Bybit Account client (read-only).
     */
    public BybitApiAsyncAccountRestClient getReadOnlyAccountClient(Long userId, String accountName) {
        return getReadOnlyClientFactory(userId, accountName).newAsyncAccountRestClient();
    }

    /**
     * Returns a user-specific Bybit Account client (read-write).
     */
    public BybitApiAsyncAccountRestClient getReadWriteAccountClient(Long userId, String accountName) {
        return getReadWriteClientFactory(userId, accountName).newAsyncAccountRestClient();
    }

    /**
     * Returns a user-specific Bybit Trade client (read-write only).
     */
    public BybitApiAsyncTradeRestClient getTradeClient(Long userId, String accountName) {
        return getReadWriteClientFactory(userId, accountName).newAsyncTradeRestClient();
    }

    /**
     * Returns a user-specific Bybit Position client (read-write only).
     */
    public BybitApiAsyncPositionRestClient getPositionClient(Long userId, String accountName) {
        return getReadWriteClientFactory(userId, accountName).newAsyncPositionRestClient();
    }
}
