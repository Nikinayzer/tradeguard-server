package korn03.tradeguardserver.endpoints.controller.news;

import korn03.tradeguardserver.endpoints.dto.news.CryptoPanicFrontendResponse;
import korn03.tradeguardserver.service.news.CryptoPanicService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
public class NewsController {

    private final CryptoPanicService cryptoPanicService;

    public NewsController(CryptoPanicService cryptoPanicService) {
        this.cryptoPanicService = cryptoPanicService;
    }

    /**
     * GET general news about crypto
     * @param page page for pagination
     * @return DTO with news
     */
    @GetMapping
    public CryptoPanicFrontendResponse getGeneralNews(@RequestParam(required = false) Integer page) {
        return cryptoPanicService.getGeneralNews(page);
    }

    /**
     * GET news about specific coin
     * @param coin coin to fetch news
     * @param page page for pagination
     * @return DTO with news
     */
    @GetMapping("/{coin}")
    public CryptoPanicFrontendResponse getCoinNews(
            @PathVariable String coin,
            @RequestParam(required = false) Integer page) {
        return cryptoPanicService.getCoinNews(coin, page);
    }
} 