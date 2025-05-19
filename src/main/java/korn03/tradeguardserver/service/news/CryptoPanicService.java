package korn03.tradeguardserver.service.news;

import korn03.tradeguardserver.endpoints.dto.news.CryptoPanicResponse;
import korn03.tradeguardserver.endpoints.dto.news.CryptoPanicFrontendResponse;
import korn03.tradeguardserver.service.core.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Service
public class CryptoPanicService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoPanicService.class);
    private static final String API_BASE_URL = "https://cryptopanic.com/api/v1/posts/";
    private static final Duration CACHE_DURATION = Duration.ofHours(1);

    private final RestTemplate restTemplate;
    private final CacheService cacheService;

    @Value("${news.api.cryptopanic}")
    private String apiKey;

    public CryptoPanicService(CacheService cacheService) {
        this.restTemplate = new RestTemplate();
        this.cacheService = cacheService;
    }

    public CryptoPanicFrontendResponse getGeneralNews(Integer page) {
        String cacheKey = "news:general:" + (page != null ? page : 1);
        CryptoPanicResponse cachedResponse = cacheService.getFromCache(cacheKey, CryptoPanicResponse.class);
        if (cachedResponse != null) {
            logger.info("Returning cached general news for page {}", page);
            return CryptoPanicFrontendResponse.from(cachedResponse);
        }

        String url = buildUrl(null, null, page);
        CryptoPanicResponse response = fetchNews(url);
        if (response != null) {
            cacheService.storeInCache(cacheKey, response, CACHE_DURATION);
            return CryptoPanicFrontendResponse.from(response);
        }
        return new CryptoPanicFrontendResponse();
    }

    public CryptoPanicFrontendResponse getCoinNews(String coin, Integer page) {
        String cacheKey = "news:" + coin.toLowerCase() + ":" + (page != null ? page : 1);
        CryptoPanicResponse cachedResponse = cacheService.getFromCache(cacheKey, CryptoPanicResponse.class);
        if (cachedResponse != null) {
            logger.info("Returning cached news for coin: {} page {}", coin, page);
            return CryptoPanicFrontendResponse.from(cachedResponse);
        }

        String url = buildUrl(coin, null, page);
        CryptoPanicResponse response = fetchNews(url);
        if (response != null) {
            cacheService.storeInCache(cacheKey, response, CACHE_DURATION);
            return CryptoPanicFrontendResponse.from(response);
        }
        return new CryptoPanicFrontendResponse();
    }

    private String buildUrl(String coin, String filter, Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(API_BASE_URL)
                .queryParam("auth_token", apiKey)
                .queryParam("public", "true")
                .queryParam("regions", "en");

        if (coin != null) {
            builder.queryParam("currencies", coin.toUpperCase());
        }
        if (filter != null) {
            builder.queryParam("filter", filter);
        }
        if (page != null) {
            builder.queryParam("page", page);
        }

        return builder.toUriString();
    }

    private CryptoPanicResponse fetchNews(String url) {
        try {
            ResponseEntity<CryptoPanicResponse> response = restTemplate.getForEntity(url, CryptoPanicResponse.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching news from CryptoPanic: {}", e.getMessage(), e);
            return null;
        }
    }
} 