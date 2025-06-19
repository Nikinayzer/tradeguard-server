package korn03.tradeguardserver.scheduler;

import static korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider.BINANCE_DEMO;
import static korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider.BINANCE_LIVE;
import static korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider.BYBIT_DEMO;
import static korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider.BYBIT_LIVE;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import korn03.tradeguardserver.model.entity.user.Role;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.model.repository.user.connections.UserBybitAccountRepository;
import korn03.tradeguardserver.service.core.EncryptionService;
import korn03.tradeguardserver.service.user.UserService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import korn03.tradeguardserver.service.user.connection.UserExchangeAccountService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UsersScheduler {

    private final UserService userService;
    private final UserExchangeAccountService exchangeAccountService;
    private final UserDiscordAccountService discordAccountService;
    private final UserBybitAccountRepository accountRepository;
    private final EncryptionService encryptionService;

    @Value("${tradeguard.default.user.username}")
    private String userUsername;
    @Value("${tradeguard.default.user.password}")
    private String userPassword;
    @Value("${tradeguard.default.admin.username}")
    private String adminUsername;
    @Value("${tradeguard.default.admin.password}")
    private String adminPassword;

    // ADMIN LIVE ?
    @Value("${tradeguard.default.bybit.live.key:}")
    private String defaultReadWriteApiKey;
    @Value("${tradeguard.default.bybit.live.secret:}")
    private String defaultReadWriteApiSecret;

    // ADMIN DEMO
    @Value("${tradeguard.default.bybit.demo.key:}")
    private String defaultDemoReadWriteApiKey;
    @Value("${tradeguard.default.bybit.demo.secret:}")
    private String defaultDemoReadWriteApiSecret;

    // MADY TESTING
    @Value("${tradeguard.default.bybit.testerMady.key:}")
    private String madyKey;
    @Value("${tradeguard.default.bybit.testerMady.secret:}")
    private String madySecret;

    // MISHA TESTING
    @Value("${tradeguard.default.bybit.testerMisha.key:}")
    private String mishaKey;
    @Value("${tradeguard.default.bybit.testerMisha.secret:}")
    private String mishaSecret;

    // BYBIT TODO ORDER
    @Value("${tradeguard.default.binance.readwrite.key:}")
    private String defaultNanceReadWriteApiKey;
    @Value("${tradeguard.default.binance.readwrite.secret:}")
    private String defaultNanceReadWriteApiSecret;
    @Value("${tradeguard.default.binance.demo.key:}")
    private String defaultNanceDemoApiKey;
    @Value("${tradeguard.default.binance.demo.secret:}")
    private String defaultNanceDemoApiSecret;

    public UsersScheduler(UserService userService, UserExchangeAccountService exchangeAccountService, UserDiscordAccountService discordAccountService, UserBybitAccountRepository accountRepository, EncryptionService encryptionService) {
        this.userService = userService;
        this.exchangeAccountService = exchangeAccountService;
        this.discordAccountService = discordAccountService;
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Initializing default users and their Exchange & Discord accounts
     */
    @PostConstruct
    public void initDefaultUser() {
        // Handle regular user
        User existingOrNewUser;
        if (!userService.userExistsByUsername(userUsername)) {
            User user = new User();
            user.setUsername(userUsername);
            user.setPassword(userPassword);
            user.setFirstName("Marcel");
            user.setLastName("Valový");
            user.setDateOfBirth(LocalDate.parse("2001-01-01")); // Example date, adjust as needed
            user.setEmailVerified(true);
            user.setEmail("marcel.valovy@vse.cz");
            user.setRegisteredAt(Instant.now());
            existingOrNewUser = userService.createUser(user);

            log.info("################# DEFAULT USER BEING CREATED #################");
            log.info("Creating new default user: {} with ID: {}", userUsername, existingOrNewUser.getId());
        } else {
            existingOrNewUser = userService.findByUsername(userUsername).orElseThrow();
            log.info("############## UPDATING EXISTING USER CONNECTIONS ############");
            log.info("Updating existing user's connections: {} with ID: {}", userUsername, existingOrNewUser.getId());
        }

//        createDefaultAccount(existingOrNewUser.getId(), "MVBb-Demo", BYBIT_DEMO);
//        createDefaultAccount(existingOrNewUser.getId(), "MVNc-Demo", BINANCE_DEMO);
//        createDefaultAccount(existingOrNewUser.getId(), "MVBb-Live", BYBIT_LIVE);
//        createDefaultAccount(existingOrNewUser.getId(), "MVNc-Live", BINANCE_LIVE);

        log.info("##############################################################");

        createDefaultDiscordAccount(existingOrNewUser.getId(), 238283760540450816L, "marcelv3612", "");

        // Handle admin user
        User adminUser;
        if (!userService.userExistsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(adminPassword);
            admin.setFirstName("Misha");
            admin.setLastName("Bubnov");
            admin.setDateOfBirth(LocalDate.parse("2002-02-21"));
            admin.setEmail("bubnov.mykhailo.cz@gmail.com");
            admin.setEmailVerified(true);
            admin.setTwoFactorEnabled(false);
            admin.setRegisteredAt(Instant.now());
            adminUser = userService.createUser(admin);
            userService.addUserRole(adminUser.getId(), Role.ADMIN);

            log.info("############### DEFAULT ADMIN BEING CREATED #################");
            log.info("Creating default admin: {} with ID: {}", adminUsername, adminUser.getId());
        } else {
            adminUser = userService.findByUsername(adminUsername).orElseThrow();
            log.info("############ UPDATING EXISTING ADMIN CONNECTIONS ############");
            log.info("Updating connections for admin: {} with ID: {}", adminUsername, adminUser.getId());
        }
        createDefaultAccount(adminUser.getId(), "Admin-Demo-Bybit", mishaKey, mishaSecret, BYBIT_DEMO);
        //createDefaultAccount(adminUser.getId(), "Admin-Demo-Bybit", defaultDemoReadWriteApiKey, defaultDemoReadWriteApiSecret, BYBIT_DEMO);
        createDefaultDiscordAccount(adminUser.getId(), 493077349684740097L, "n1ckor", "c711e2e7b4b31f475e0fa51dc5bed1dc");
    }

    /**
     * Creates or updates a default Exchange account for a user if API keys are provided in properties
     */
    private void createDefaultAccount(Long userId, String accountName, String key, String secret, ExchangeProvider provider) {
        try {
//            String readWriteApiKey = switch (provider) {
//                case BYBIT_LIVE -> defaultReadWriteApiKey;
//                case BYBIT_DEMO -> defaultDemoReadWriteApiKey;
//                case BINANCE_LIVE -> defaultNanceReadWriteApiKey;
//                case BINANCE_DEMO -> defaultNanceDemoApiKey;
//            };
//
//            String readWriteApiSecret = switch (provider) {
//                case BYBIT_LIVE -> defaultReadWriteApiSecret;
//                case BYBIT_DEMO -> defaultDemoReadWriteApiSecret;
//                case BINANCE_LIVE -> defaultNanceReadWriteApiSecret;
//                case BINANCE_DEMO -> defaultNanceDemoApiSecret;
//            };

            // Get all existing accounts for this user
            List<UserExchangeAccount> existingAccounts = accountRepository.findByUserId(userId);

            boolean accountExists = false;
            for (UserExchangeAccount existingAccount : existingAccounts) {
                if (existingAccount.getAccountName().equals(accountName)) {
                    existingAccount.setProvider(provider);
                    existingAccount.setEncryptedReadWriteApiKey(encryptionService.encrypt(key));
                    existingAccount.setEncryptedReadWriteApiSecret(encryptionService.encrypt(secret));
                    accountRepository.save(existingAccount);
                    log.info("Updated existing {} account for user ID: {}", provider, userId);
                    accountExists = true;
                    break;
                }
            }

            // If no existing account was found, create a new one
            if (!accountExists) {
                exchangeAccountService.saveExchangeAccount(
                        userId,
                        accountName,
                        provider,
                        key,
                        secret
                );
                log.info("Created new {} account for user ID: {}", provider, userId);
            }
        } catch (Exception e) {
            log.error("Failed to create/update {} account for user ID: {}", provider, userId, e);
        }
//        } else {
//            log.warn("Skipping {} account creation/update - API keys not provided in properties", provider);
//        }
    }

    /**
     * Creates a default Discord account for a user (hardcoded values) if it doesn't already exist
     */
    private void createDefaultDiscordAccount(Long userId, Long discordId, String discordUsername, String discordAvatar) {
        try {
            // Check if discord account already exists for this user
            boolean accountExists = discordAccountService.getDiscordAccount(userId)
                    .map(account -> account.getDiscordId().equals(discordId))
                    .orElse(false);

            if (accountExists) {
                log.info("Discord account already exists for user ID: {}, Discord ID: {}", userId, discordId);
                return;
            }

            discordAccountService.addDiscordAccount(userId, discordId, discordUsername, discordAvatar);
            log.info("Default Discord account created for user ID: {}, Discord ID: {}", userId, discordId);
        } catch (Exception e) {
            log.error("`Failed to create` default Discord account for user ID: {}", userId, e);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // Runs daily at 3 AM
    private void cleanUpUnverifiedUsers() {
    }
}

