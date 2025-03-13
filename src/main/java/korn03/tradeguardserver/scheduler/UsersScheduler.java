package korn03.tradeguardserver.scheduler;

import jakarta.annotation.PostConstruct;
import korn03.tradeguardserver.model.entity.user.Role;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.service.user.UserBybitAccountService;
import korn03.tradeguardserver.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
@Slf4j
public class UsersScheduler {


    private final UserService userService;
    private final UserBybitAccountService bybitAccountService;

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

    public UsersScheduler(UserService userService, UserBybitAccountService bybitAccountService) {
        this.userService = userService;
        this.bybitAccountService = bybitAccountService;
    }

    /**
     * Initializing default users and their Bybit accounts
     */
    @PostConstruct
    public void initDefaultUser() {
        if (!userService.userExists(userUsername)) {
            User user = new User();
            user.setUsername(userUsername);
            user.setPassword(userPassword);
            user.setFirstName("Joe");
            user.setLastName("Biden");
            user.setEmail("joebiden@seznam.cz");
            user.setRoles(Set.of(Role.USER));
            user.setRegisteredAt(Instant.now());
            User createdUser = userService.createUser(user);
            createDefaultBybitAccount(createdUser.getId(), "Default");
        }

        if (!userService.userExists(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(adminPassword);
            admin.setFirstName("Barack");
            admin.setLastName("Obama");
            admin.setEmail("barackobama@seznam.cz");
            admin.setRoles(Set.of(Role.USER, Role.ADMIN));
            admin.setRegisteredAt(Instant.now());
            User createdAdmin = userService.createUser(admin);
            createDefaultBybitAccount(createdAdmin.getId(), "admins bybit");
        }
        log.debug("DEFAULT USERS AND BYBIT ACCOUNTS CREATED");
    }

    /**
     * Creates a default Bybit account for a user if API keys are provided in properties
     */
    private void createDefaultBybitAccount(Long userId, String accountName) {
        if (!defaultReadOnlyApiKey.isEmpty() && !defaultReadOnlyApiSecret.isEmpty() 
            && !defaultReadWriteApiKey.isEmpty() && !defaultReadWriteApiSecret.isEmpty()) {
            
            try {
                bybitAccountService.saveBybitAccount(
                    userId,
                    accountName,
                    defaultReadOnlyApiKey,
                    defaultReadOnlyApiSecret,
                    defaultReadWriteApiKey,
                    defaultReadWriteApiSecret
                );
                log.info("Default Bybit account created for user ID: {}", userId);
            } catch (Exception e) {
                log.error("Failed to create default Bybit account for user ID: {}", userId, e);
            }
        } else {
            log.warn("Skipping Bybit account creation - API keys not provided in properties");
        }
    }
}
