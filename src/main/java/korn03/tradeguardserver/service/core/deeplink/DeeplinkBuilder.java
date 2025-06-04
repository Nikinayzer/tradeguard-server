package korn03.tradeguardserver.service.core.deeplink;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class DeeplinkBuilder {

    @Value("${spring.application.deeplink-prefix}")
    private String deeplinkPrefix;

    public String build(DeeplinkRoute route) {
        return build(route, null);
    }

    public String build(DeeplinkRoute route, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(deeplinkPrefix + route.getPath());

        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(builder::queryParam);
        }

        return builder.toUriString();
    }
}