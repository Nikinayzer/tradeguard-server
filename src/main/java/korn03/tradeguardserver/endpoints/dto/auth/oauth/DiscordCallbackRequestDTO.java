package korn03.tradeguardserver.endpoints.dto.auth.oauth;

import lombok.Data;

@Data
public class DiscordCallbackRequestDTO {
    private String code;
    private String codeVerifier;
}
