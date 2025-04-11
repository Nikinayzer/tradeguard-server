package korn03.tradeguardserver.service;

import korn03.tradeguardserver.endpoints.dto.internal.UserConnectionsFromDiscordDTO;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import korn03.tradeguardserver.service.user.UserService;
import korn03.tradeguardserver.service.user.connection.UserExchangeAccountService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InternalService {

    private final UserDiscordAccountService discordAccountService;
    private final UserService userService;
    private final UserExchangeAccountService exchangeAccountService;

    public InternalService(UserDiscordAccountService discordAccountService, UserService userService, UserExchangeAccountService exchangeAccountService) {
        this.discordAccountService = discordAccountService;
        this.userService = userService;
        this.exchangeAccountService = exchangeAccountService;
    }

    /**
     * Retrieves user connections (User, Discord, Exchange) based on Discord ID.
     */
    public UserConnectionsFromDiscordDTO getUserConnectionsByDiscordId(Long discordId) {
        UserDiscordAccount discordAccount = discordAccountService.findByDiscordId(discordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discord account not found"));

        User user = userService.getById(discordAccount.getUserId());

        List<UserExchangeAccount> bybitAccounts = exchangeAccountService.getUserExchangeAccountsEntites(user.getId());

        List<UserConnectionsFromDiscordDTO.Exchange> exchangeDTOS = bybitAccounts.stream()
                .map(account -> UserConnectionsFromDiscordDTO.Exchange.builder()
                        .id(String.valueOf(account.getId())) //todo think what we can do with String.valueOf()?
                        .name(account.getAccountName())
                        .provider(account.getProvider().name())
                        .demo(account.isDemo())
                        .readOnlyApiKey(exchangeAccountService.getDecryptedReadOnlyApiKey(account))
                        .readOnlyApiSecret(exchangeAccountService.getDecryptedReadOnlyApiSecret(account))
                        .readWriteApiKey(exchangeAccountService.getDecryptedReadWriteApiKey(account))
                        .readWriteApiSecret(exchangeAccountService.getDecryptedReadWriteApiSecret(account))
                        .build())
                .toList();

        UserConnectionsFromDiscordDTO u = UserConnectionsFromDiscordDTO.builder()
                .user(UserConnectionsFromDiscordDTO.User.builder()
                        .id(String.valueOf(user.getId()))
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .build())
                .discord(UserConnectionsFromDiscordDTO.Discord.builder()
                        .discordId(String.valueOf(discordAccount.getDiscordId()))
                        .username(discordAccount.getDiscordUsername())
                        .build())
                .exchange(exchangeDTOS)
                .build();
        System.out.println(u);
        return u;
    }
}
