package korn03.tradeguardserver.service.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class NewsCacheService {


    private static final Logger logger = LoggerFactory.getLogger(NewsCacheService.class);
    private static final int NEWS_CACHE_TTL = 6;

    private final RedisTemplate<String, Object> redisTemplate;

    public NewsCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(value = "news", key = "'generalNews'")
    public List<NewsService.Article> getGeneralNews() {
        return getCachedNews("news:general");
    }

    @Cacheable(value = "news", key = "'coinNews:' + #coin")
    public List<NewsService.Article> getCoinNews(String coin) {
        return getCachedNews("news:" + coin);
    }

    public void cacheNews(String cacheKey, List<NewsService.Article> articles) {
        redisTemplate.opsForValue().set(cacheKey, articles, NEWS_CACHE_TTL, TimeUnit.HOURS);
    }

    public List<NewsService.Article> getCachedNews(String cacheKey) {
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        return cachedData instanceof List<?> ? (List<NewsService.Article>) cachedData : List.of();
    }
}
