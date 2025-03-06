package korn03.tradeguardserver.client;

import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import korn03.tradeguardserver.db.entity.BybitAccount;
import korn03.tradeguardserver.service.BybitAccountService;
import korn03.tradeguardserver.service.EncryptionService;
import org.springframework.stereotype.Component;

@Component
public class BybitApiClientManager {

    private final BybitAccountService accountService;
    private final EncryptionService encryptionService;

    public BybitApiClientManager(BybitAccountService accountService, EncryptionService encryptionService) {
        this.accountService = accountService;
        this.encryptionService = encryptionService;
    }

    public BybitApiMarketRestClient getClient(Long userId, String accountName) {
        BybitAccount account = accountService.getAccount(userId, accountName)
                .orElseThrow(() -> new RuntimeException("Bybit account not found"));

        return BybitApiClientFactory.newInstance(
                encryptionService.decrypt(account.getEncryptedApiKey()),
                encryptionService.decrypt(account.getEncryptedApiSecret())
        ).newMarketDataRestClient();
    }
}
