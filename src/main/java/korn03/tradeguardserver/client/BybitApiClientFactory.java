package korn03.tradeguardserver.client;

import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncPositionRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.service.user.connection.UserExchangeAccountService;
import org.springframework.stereotype.Component;

@Component
public class BybitApiClientFactory {

    private final UserExchangeAccountService userExchangeAccountService;

    public BybitApiClientFactory(UserExchangeAccountService userExchangeAccountService) {
        this.userExchangeAccountService = userExchangeAccountService;
    }

    /**
     * Retrieves user-specific read-only API credentials and initializes a Exchange client.
     */
    private com.bybit.api.client.service.BybitApiClientFactory getReadOnlyClientFactory(Long userId, Long id) {
        UserExchangeAccount account = userExchangeAccountService.getExchangeAccount(userId, id);

        String apiKey = userExchangeAccountService.getDecryptedReadOnlyApiKey(account);
        String apiSecret = userExchangeAccountService.getDecryptedReadOnlyApiSecret(account);

        return com.bybit.api.client.service.BybitApiClientFactory.newInstance(apiKey, apiSecret);
    }

    /**
     * Retrieves user-specific read-write API credentials and initializes a Exchange client.
     */
    private com.bybit.api.client.service.BybitApiClientFactory getReadWriteClientFactory(Long userId, Long id) {
        UserExchangeAccount account = userExchangeAccountService.getExchangeAccount(userId, id);

        String apiKey = userExchangeAccountService.getDecryptedReadWriteApiKey(account);
        String apiSecret = userExchangeAccountService.getDecryptedReadWriteApiSecret(account);

        return com.bybit.api.client.service.BybitApiClientFactory.newInstance(apiKey, apiSecret);
    }

    /**
     * Returns a user-specific Exchange Account client (read-only).
     */
    public BybitApiAsyncAccountRestClient getReadOnlyAccountClient(Long userId, Long id) {
        return getReadOnlyClientFactory(userId, id).newAsyncAccountRestClient();
    }

    /**
     * Returns a user-specific Exchange Account client (read-write).
     */
    public BybitApiAsyncAccountRestClient getReadWriteAccountClient(Long userId, Long id) {
        return getReadWriteClientFactory(userId, id).newAsyncAccountRestClient();
    }

    /**
     * Returns a user-specific Exchange Trade client (read-write only).
     */
    public BybitApiAsyncTradeRestClient getTradeClient(Long userId, Long id) {
        return getReadWriteClientFactory(userId, id).newAsyncTradeRestClient();
    }

    /**
     * Returns a user-specific Exchange Position client (read-write only).
     */
    public BybitApiAsyncPositionRestClient getPositionClient(Long userId, Long id) {
        return getReadWriteClientFactory(userId, id).newAsyncPositionRestClient();
    }
}
