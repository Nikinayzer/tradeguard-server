package korn03.tradeguardserver.config;

import com.bybit.api.client.config.BybitApiConfig;
import com.bybit.api.client.restApi.BybitApiAsyncAccountRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncPositionRestClient;
import com.bybit.api.client.restApi.BybitApiAsyncTradeRestClient;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BybitConfig {

    @Value("${bybit.api.key}")
    private String apiKey;

    @Value("${bybit.api.secret}")
    private String apiSecret;

    @Value("${bybit.api.debug}")
    private boolean DEBUG_MODE;

    private BybitApiClientFactory createBybitClient() {
        return BybitApiClientFactory.newInstance(
                apiKey,
                apiSecret,
                BybitApiConfig.MAINNET_DOMAIN,
                DEBUG_MODE
        );
    }
    @Bean
    public BybitApiMarketRestClient bybitApiMarketRestClient() {
        return BybitApiClientFactory.newInstance(
                BybitApiConfig.MAINNET_DOMAIN,
                DEBUG_MODE
        ).newMarketDataRestClient();
    }

    @Bean
    public BybitApiAsyncTradeRestClient bybitAsyncTradeRestClient() {
        return createBybitClient().newAsyncTradeRestClient();
    }

    @Bean
    public BybitApiAsyncPositionRestClient bybitAsyncPositionRestClient() {
        return createBybitClient().newAsyncPositionRestClient();
    }

    @Bean
    public BybitApiAsyncAccountRestClient bybitAsyncAccountRestClient() {
        return createBybitClient().newAsyncAccountRestClient();
    }
}
