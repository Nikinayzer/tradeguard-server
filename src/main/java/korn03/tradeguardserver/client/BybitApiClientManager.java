package korn03.tradeguardserver.client;

import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncPositionRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import korn03.tradeguardserver.model.entity.BybitAccount;
import korn03.tradeguardserver.service.BybitAccountService;
import korn03.tradeguardserver.service.EncryptionService;
import org.springframework.stereotype.Component;

@Component
public class BybitApiClientManager {

    private final BybitAccountService bybitAccountService;
    private final EncryptionService encryptionService;

    public BybitApiClientManager(BybitAccountService bybitAccountService, EncryptionService encryptionService) {
        this.bybitAccountService = bybitAccountService;
        this.encryptionService = encryptionService;
    }

    /**
     * Retrieves user-specific API credentials and initializes a Bybit client.
     */
    private BybitApiClientFactory getClientFactory(Long userId, String accountName) {
        BybitAccount account = bybitAccountService.getAccount(userId, accountName).orElseThrow(() -> new RuntimeException("Bybit account not found"));

        String apiKey = encryptionService.decrypt(account.getEncryptedApiKey());
        String apiSecret = encryptionService.decrypt(account.getEncryptedApiSecret());

        return BybitApiClientFactory.newInstance(apiKey, apiSecret);
    }

    /**
     * Returns a user-specific Bybit Account client.
     */
    public BybitApiAsyncAccountRestClient getAccountClient(Long userId, String accountName) {
        return getClientFactory(userId, accountName).newAsyncAccountRestClient();
    }

    /**
     * Returns a user-specific Bybit Trade client.
     */
    public BybitApiAsyncTradeRestClient getTradeClient(Long userId, String accountName) {
        return getClientFactory(userId, accountName).newAsyncTradeRestClient();
    }

    /**
     * Returns a user-specific Bybit Position client.
     */
    public BybitApiAsyncPositionRestClient getPositionClient(Long userId, String accountName) {
        return getClientFactory(userId, accountName).newAsyncPositionRestClient();
    }
}
