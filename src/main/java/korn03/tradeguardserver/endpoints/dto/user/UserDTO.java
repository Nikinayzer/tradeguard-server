package korn03.tradeguardserver.endpoints.dto.user;

import korn03.tradeguardserver.endpoints.dto.user.exchangeAccount.ExchangeAccountDTO;
import korn03.tradeguardserver.model.entity.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Instant registeredAt;
    private Instant updatedAt;
    private Set<Role> roles;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

   private DiscordAccountDTO discordAccount;
    @Data
    @Builder
    public static class DiscordAccountDTO{
        private String discordId;
        private String username;
        private String discriminator;
        private String avatar;
    }
    private List<ExchangeAccountDTO> exchangeAccounts;
}