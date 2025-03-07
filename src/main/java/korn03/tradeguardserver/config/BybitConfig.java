package korn03.tradeguardserver.config;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncPositionRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BybitConfig {
    private boolean DEBUG_MODE;

    @Bean
    public BybitApiMarketRestClient bybitApiMarketRestClient() {
        return BybitApiClientFactory.newInstance(
                BybitApiConfig.MAINNET_DOMAIN,
                DEBUG_MODE
        ).newMarketDataRestClient();
    }

}
