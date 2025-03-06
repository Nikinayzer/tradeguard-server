package korn03.tradeguardserver.endpoints.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PingController {

    @GetMapping("/ping")
    public String test() {
        return "Hello World, it's TradeGuard!";
    }
    @GetMapping("/pingsecure")
    //@PreAuthorize("hasRole('ADMIN')")
    public String testSecure() {
        return "Hello World, it's TradeGuard, but secured!";
    }


}
