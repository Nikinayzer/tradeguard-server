package korn03.tradeguardserver.service;

import korn03.tradeguardserver.endpoints.dto.internal.UserConnectionsFromDiscordDTO;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.UserBybitAccount;
import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import korn03.tradeguardserver.service.user.UserService;
import korn03.tradeguardserver.service.user.connection.UserBybitAccountService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InternalService {

    private final UserDiscordAccountService discordAccountService;
    private final UserService userService;
    private final UserBybitAccountService bybitAccountService;
    private final UserBybitAccountService userBybitAccountService;

    public InternalService(UserDiscordAccountService discordAccountService, UserService userService, UserBybitAccountService bybitAccountService, UserBybitAccountService userBybitAccountService) {
        this.discordAccountService = discordAccountService;
        this.userService = userService;
        this.bybitAccountService = bybitAccountService;
        this.userBybitAccountService = userBybitAccountService;
    }

    /**
     * Retrieves user connections (User, Discord, Bybit) based on Discord ID.
     */
    public UserConnectionsFromDiscordDTO getUserConnectionsByDiscordId(Long discordId) {
        UserDiscordAccount discordAccount = discordAccountService.findByDiscordId(discordId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discord account not found"));

        User user = userService.getById(discordAccount.getUserId());

        List<UserBybitAccount> bybitAccounts = bybitAccountService.getUserBybitAccounts(user.getId());

        List<UserConnectionsFromDiscordDTO.Bybit> bybitDTOs = bybitAccounts.stream()
                .map(account -> UserConnectionsFromDiscordDTO.Bybit.builder()
                        .id(String.valueOf(account.getId())) //todo think what we can do with String.valueOf()?
                        .name(account.getAccountName())
                        .readOnlyApiKey(userBybitAccountService.getDecryptedReadOnlyApiKey(account))
                        .readOnlyApiSecret(userBybitAccountService.getDecryptedReadOnlyApiSecret(account))
                        .readWriteApiKey(userBybitAccountService.getDecryptedReadWriteApiKey(account))
                        .readWriteApiSecret(userBybitAccountService.getDecryptedReadWriteApiSecret(account))
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
                .bybit(bybitDTOs)
                .build();
        System.out.println(u);
        return u;
    }
}
