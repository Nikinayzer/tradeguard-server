package korn03.tradeguardserver.endpoints.dto.internal;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserConnectionsDTO {
    private User user;
    private Discord discord;
    private List<Exchange> exchangeClients;

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
    public static class Exchange {
        private String id;
        private String provider;
        private String name;
        private Boolean demo;
        private String readOnlyApiKey;
        private String readOnlyApiSecret;
        private String readWriteApiKey;
        private String readWriteApiSecret;
    }
}
