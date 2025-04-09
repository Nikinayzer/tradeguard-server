package korn03.tradeguardserver.endpoints.dto.auth.oauth;

import lombok.*;

@Setter
@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscordUserDTO {
    private String id;
    private String username;
    private String email;
    private String discriminator;
    private String avatar; //hash of the avatar image on Discord cdn
}