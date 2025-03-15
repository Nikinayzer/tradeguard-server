package korn03.tradeguardserver.endpoints.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscordAccountDTO {
    private Long id;
    private Long userId;
    private Long discordId;
    private String accountName;
}
