package korn03.tradeguardserver.endpoints.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class NewsController {

    @GetMapping("/news")
    public String news() {
        return "There will be news";
    }
}
