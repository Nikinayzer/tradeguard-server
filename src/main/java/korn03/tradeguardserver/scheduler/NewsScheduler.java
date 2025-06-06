package korn03.tradeguardserver.scheduler;

import jakarta.annotation.PostConstruct;
import korn03.tradeguardserver.service.news.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NewsScheduler.class);
    private final NewsService newsService;

    public NewsScheduler(NewsService newsService) {
        this.newsService = newsService;
    }

    //@PostConstruct
    public void runOnStartup() {
        logger.info("Fetching news on startup and storing mentions...");
        updateNewsData();
    }

    //@Scheduled(cron = "0 0 2 * * ?") // Runs daily at 2 AM
    public void scheduledTask() {
        logger.info("Scheduled: Fetching news and storing mentions...");
        updateNewsData();
    }

    private void updateNewsData() {
        try {
            logger.info("Fetching specific coin news...");
            newsService.fetchCoinSpecificNews("BTC");
            newsService.fetchCoinSpecificNews("ETH");
            logger.info("News fetching and caching completed successfully.");
        } catch (Exception e) {
            logger.error("Error fetching news: {}", e.getMessage(), e);
        }
    }
}
