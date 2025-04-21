package korn03.tradeguardserver.scheduler;

import jakarta.annotation.PostConstruct;
import korn03.tradeguardserver.model.entity.user.Role;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider;
import korn03.tradeguardserver.service.user.connection.UserExchangeAccountService;
import korn03.tradeguardserver.service.user.UserService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

import static korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider.BINANCE_LIVE;
import static korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider.BYBIT_DEMO;
import static korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider.BYBIT_LIVE;

@Component
@Slf4j
public class UsersScheduler {

    private final UserService userService;
    private final UserExchangeAccountService exchangeAccountService;
    private final UserDiscordAccountService discordAccountService;

    @Value("${tradeguard.default.user.username}")
    private String userUsername;
    @Value("${tradeguard.default.user.password}")
    private String userPassword;
    @Value("${tradeguard.default.admin.username}")
    private String adminUsername;
    @Value("${tradeguard.default.admin.password}")
    private String adminPassword;

    @Value("${tradeguard.default.bybit.readonly.key:}")
    private String defaultReadOnlyApiKey;
    @Value("${tradeguard.default.bybit.readonly.secret:}")
    private String defaultReadOnlyApiSecret;
    @Value("${tradeguard.default.bybit.readwrite.key:}")
    private String defaultReadWriteApiKey;
    @Value("${tradeguard.default.bybit.readwrite.secret:}")
    private String defaultReadWriteApiSecret;
    @Value("${tradeguard.default.bybit.demo.key:}")
    private String defaultDemoReadWriteApiKey;
    @Value("${tradeguard.default.bybit.demo.secret:}")
    private String defaultDemoReadWriteApiSecret;
    @Value("${tradeguard.default.binance.readwrite.key:}")
    private String defaultNanceReadWriteApiKey;
    @Value("${tradeguard.default.binance.readwrite.secret:}")
    private String defaultNanceReadWriteApiSecret;

    @Value("${tradeguard.run-mode:demo}")
    private String runMode;

    public UsersScheduler(UserService userService, UserExchangeAccountService exchangeAccountService, UserDiscordAccountService discordAccountService) {
        this.userService = userService;
        this.exchangeAccountService = exchangeAccountService;
        this.discordAccountService = discordAccountService;
    }

    /**
     * Initializing default users and their Exchange & Discord accounts
     */
    @PostConstruct
    public void initDefaultUser() {
        if (!userService.userExists(userUsername)) {
            User user = new User();
            user.setUsername(userUsername);
            user.setPassword(userPassword);
            user.setFirstName("Marcel");
            user.setLastName("Valovy");
            user.setEmail("marcel.valovy@vse.cz");
//            user.setRoles(Set.of(Role.USER, Role.ADMIN));
            user.setRegisteredAt(Instant.now());
            User createdUser = userService.createUser(user);

            // Create default accounts based on run mode
            if ("demo".equalsIgnoreCase(runMode)) {
                createDefaultAccount(createdUser.getId(), "MVBb-Demo", BYBIT_DEMO);
                log.info("Created DEMO account for user as RUN_MODE is set to: {}", runMode);
            } else if ("live".equalsIgnoreCase(runMode)) {
                createDefaultAccount(createdUser.getId(), "MVBb", BYBIT_LIVE);
                createDefaultAccount(createdUser.getId(), "MVNc", BINANCE_LIVE);
                log.info("Created LIVE accounts for user as RUN_MODE is set to: {}", runMode);
            } else {
                log.warn("Unknown RUN_MODE value: {}. No default exchange accounts created.", runMode);
            }

            createDefaultDiscordAccount(createdUser.getId(), 238283760540450816L, "marcelv3612", "");
        }

        if (!userService.userExists(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(adminPassword);
            admin.setFirstName("Nick");
            admin.setLastName("Korotov");
            admin.setEmail("korotov.nick@gmail.com");
            //admin.setEmail("korn03@vse.cz");
//            admin.setRoles(Set.of(Role.USER, Role.ADMIN));
            admin.setRegisteredAt(Instant.now());
            User createdAdmin = userService.createUser(admin);
            userService.addUserRole(createdAdmin.getId(), Role.ADMIN);

            // Create default accounts based on run mode
            if ("demo".equalsIgnoreCase(runMode)) {
                createDefaultAccount(createdAdmin.getId(), "Admin-Demo-Bybit", BYBIT_DEMO);
                log.info("Created DEMO account for admin as RUN_MODE is set to: {}", runMode);
            } else if ("live".equalsIgnoreCase(runMode)) {
                createDefaultAccount(createdAdmin.getId(), "Admin-Bybit", BYBIT_LIVE);
//                createDefaultAccount(createdAdmin.getId(), "Admin-Binance", BINANCE_LIVE);
                log.info("Created LIVE accounts for admin as RUN_MODE is set to: {}", runMode);
            } else {
                log.warn("Unknown RUN_MODE value: {}. No default exchange accounts created for admin.", runMode);
            }

            createDefaultDiscordAccount(createdAdmin.getId(), 493077349684740097L, "n1ckor", "c711e2e7b4b31f475e0fa51dc5bed1dc");
        }
        log.debug("DEFAULT USERS, BYBIT ACCOUNTS, AND DISCORD ACCOUNTS CREATED");
    }

    /**
     * Creates a default Exchange account for a user if API keys are provided in properties
     */
    private void createDefaultAccount(Long userId, String accountName, ExchangeProvider provider) {
        if (!defaultReadOnlyApiKey.isEmpty() && !defaultReadOnlyApiSecret.isEmpty()
                && !defaultReadWriteApiKey.isEmpty() && !defaultReadWriteApiSecret.isEmpty()) {

            try {
                String readWriteApiKey = switch (provider) {
                    case BYBIT_LIVE -> defaultReadWriteApiKey;
                    case BYBIT_DEMO -> defaultDemoReadWriteApiKey;
                    case BINANCE_LIVE -> defaultNanceReadWriteApiKey;
                };

                String readWriteApiSecret = switch (provider) {
                    case BYBIT_LIVE -> defaultReadWriteApiSecret;
                    case BYBIT_DEMO -> defaultDemoReadWriteApiSecret;
                    case BINANCE_LIVE -> defaultNanceReadWriteApiSecret;
                };

                exchangeAccountService.saveExchangeAccount(
                        userId,
                        accountName,
                        provider,
                        defaultReadOnlyApiKey,
                        defaultReadOnlyApiSecret,
                        readWriteApiKey,
                        readWriteApiSecret
                );
                log.info("Default {} account created for user ID: {}", provider, userId);
            } catch (Exception e) {
                log.error("Failed to create default {} account for user ID: {}", provider, userId, e);
            }
        } else {
            log.warn("Skipping {} account creation - API keys not provided in properties", provider);
        }
    }

    /**
     * Creates a default Discord account for a user (hardcoded values)
     */
    private void createDefaultDiscordAccount(Long userId, Long discordId, String discordUsername, String discordAvatar) {
        try {
            discordAccountService.addDiscordAccount(userId, discordId, discordUsername, discordAvatar);
            log.info("Default Discord account created for user ID: {}, Discord ID: {}", userId, discordId);
        } catch (Exception e) {
            log.error("Failed to create default Discord account for user ID: {}", userId, e);
        }
    }
}
