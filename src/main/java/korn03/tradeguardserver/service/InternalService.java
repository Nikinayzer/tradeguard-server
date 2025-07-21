package korn03.tradeguardserver.service;

import korn03.tradeguardserver.endpoints.dto.internal.UserConnectionsDTO;
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
     * !NOTE: DISCORD ID <10000 IS TEMP FIX OF TRADING ENGINE LOGIC (IT REQUIRES VALID DISCORD ID, BUT HERE WE TWEAK WITH USER ID)
     */
    public UserConnectionsDTO getUserConnectionsByDiscordId(Long discordId) {

        Long userId;
        Long discordIdDTO;
        String discordUsername;

        if (discordId < 10000) { // assuming discord ID cant be less than 10000, it's internal ID from .env which is fed to trading engine
            userId = discordId;
            discordIdDTO = userId;
            discordUsername = userService.getById(userId).getUsername();
        } else {
            UserDiscordAccount discordAccount = discordAccountService.findByDiscordId(discordId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Discord account not found"));
            userId = discordAccount.getUserId();
            discordIdDTO = discordAccount.getDiscordId();
            discordUsername = discordAccount.getDiscordUsername();
        }

        User user = userService.getById(userId);
        List<UserExchangeAccount> bybitAccounts = exchangeAccountService.getUserExchangeAccountsEntites(user.getId());
        List<UserConnectionsDTO.Exchange> exchangeDTOS = bybitAccounts.stream()
                .map(account -> UserConnectionsDTO.Exchange.builder()
                        .id(String.valueOf(account.getId()))
                        .name(account.getAccountName())
                        .provider(account.getProvider().name())
                        .readOnlyApiKey(exchangeAccountService.getDecryptedReadWriteApiKey(account))
                        .readOnlyApiSecret(exchangeAccountService.getDecryptedReadWriteApiSecret(account))
                        .readWriteApiKey(exchangeAccountService.getDecryptedReadWriteApiKey(account))
                        .readWriteApiSecret(exchangeAccountService.getDecryptedReadWriteApiSecret(account))
                        .build())
                .toList();

        UserConnectionsDTO u = UserConnectionsDTO.builder()
                .user(UserConnectionsDTO.User.builder()
                        .id(String.valueOf(user.getId()))
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .build())
                .discord(UserConnectionsDTO.Discord.builder()
                        .discordId(String.valueOf(discordIdDTO))
                        .username(discordUsername)
                        .build())
                .exchangeClients(exchangeDTOS)
                .build();
        System.out.println(u);
        return u;
    }
}
