package korn03.tradeguardserver.endpoints.dto.internal;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserConnectionsFromDiscordDTO {
    private User user;
    private Discord discord;
    private List<Bybit> bybit;

    @Data
    @Builder
    public static class User {
        private String id;
        private String email;
        private String username;
    }

    @Data
    @Builder
    public static class Discord {
        private String discordId;
        private String username;
    }

    @Data
    @Builder
    public static class Bybit {
        private String id;
        private String name;
        private String readOnlyApiKey;
        private String readOnlyApiSecret;
        private String readWriteApiKey;
        private String readWriteApiSecret;
    }
}
