package korn03.tradeguardserver.endpoints.dto.auth.oauth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordExchangeRequestDTO {
    private String code;
    private String codeVerifier;
}
