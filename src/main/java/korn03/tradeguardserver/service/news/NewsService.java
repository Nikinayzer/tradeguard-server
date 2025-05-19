package korn03.tradeguardserver.service.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import korn03.tradeguardserver.model.entity.NewsMention;
import korn03.tradeguardserver.model.repository.NewsMentionRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//todo refactor
@Service
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);
    private static final String API_URL = "https://newsdata.io/api/1/latest";
    private static final int MAX_PAGES = 3;
    private final RestTemplate restTemplate = new RestTemplate();
    private final NewsCacheService cacheService;
    private final NewsMentionRepository newsMentionRepository;
    @Value("${news.api.newsdata}")
    private String NEWSDATA_API_KEY;

    public NewsService(NewsCacheService cacheService, NewsMentionRepository newsMentionRepository) {
        this.cacheService = cacheService;
        this.newsMentionRepository = newsMentionRepository;
    }

    public List<Article> fetchGeneralCryptoNews() {
        List<Article> cachedNews = cacheService.getGeneralNews();
        if (!cachedNews.isEmpty()) {
            logger.info("Returning cached general news...");
            return cachedNews;
        }

        List<Article> articles = fetchNewsFromAPI("crypto", MAX_PAGES);
        cacheService.cacheNews("news:general", articles);
        return articles;
    }

    public List<Article> fetchCoinSpecificNews(String coin) {
        List<Article> cachedNews = cacheService.getCoinNews(coin);
        if (!cachedNews.isEmpty()) {
            logger.info("Returning cached news for coin: {}", coin);
            return cachedNews;
        }

        List<Article> articles = fetchNewsFromAPI(coin, MAX_PAGES);
        cacheService.cacheNews("news:" + coin, articles);
        return articles;
    }

    private List<Article> fetchNewsFromAPI(String keyword, int maxPages) {
        try {
            List<Article> allArticles = new ArrayList<>();
            String nextPage = null;
            boolean isCoinSpecific = !keyword.equalsIgnoreCase("crypto");

            for (int i = 0; i < maxPages; i++) {
                String url = API_URL + "?apikey=" + NEWSDATA_API_KEY + "&q=" + keyword + "&removeduplicate=1&language=en";
                if (nextPage != null) {
                    url += "&page=" + nextPage;
                }

                ResponseEntity<NewsApiResponse> response = restTemplate.getForEntity(url, NewsApiResponse.class);

                response.getBody();
                if (response.getBody().getResults() != null) {
                    if (i == 0 && isCoinSpecific) {
                        saveNewsMentions(keyword, response.getBody().getTotalResults());
                    }
                    allArticles.addAll(response.getBody().getResults());
                    nextPage = response.getBody().getNextPage();
                }

                if (nextPage == null) break;
            }
            return allArticles;
        } catch (Exception e) {
            logger.error("Error fetching news: {}", e.getMessage(), e);
        }
        return List.of();
    }

    private void saveNewsMentions(String coin, int mentionCount) {
        LocalDate today = LocalDate.now();
        NewsMention newsMention = new NewsMention(coin, today, mentionCount);

        newsMentionRepository.save(newsMention);
        logger.info("Saved {} mentions for {} on {}", mentionCount, coin, today);
    }

    @Getter
    private static class NewsApiResponse {
        private List<Article> results;
        private String nextPage;
        private int totalResults;

    }

    @Setter
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Article implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        // Getters and Setters
        private String article_id;
        private String title;
        private String link;
        private String description;
        private String pubDate;
        private String image_url;
        private List<String> keywords;

    }
}
