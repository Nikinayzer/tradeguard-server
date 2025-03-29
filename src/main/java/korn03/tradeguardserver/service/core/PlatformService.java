package korn03.tradeguardserver.service.core;

import org.springframework.stereotype.Service;

@Service
public class PlatformService {

    public String resolveSource(String platformType) {
        if (platformType == null) return "unknown";

        return switch (platformType.toLowerCase()) {
            case "android", "ios" -> "mobile";
            case "web", "chrome", "firefox", "react" -> "web";
            default -> "unknown";
        };
    }

    public boolean isMobile(String platformType) {
        return resolveSource(platformType).equals("mobile");
    }

    public boolean isWeb(String platformType) {
        return resolveSource(platformType).equals("web");
    }

    // Room for expansion: version checks, client validation, etc.
}
