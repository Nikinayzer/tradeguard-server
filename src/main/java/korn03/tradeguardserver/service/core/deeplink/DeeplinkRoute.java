package korn03.tradeguardserver.service.core.deeplink;

import lombok.Getter;

@Getter
public enum DeeplinkRoute {
    OTP("two-factor");

    private final String path;

    DeeplinkRoute(String path) {
        this.path = path;
    }
}