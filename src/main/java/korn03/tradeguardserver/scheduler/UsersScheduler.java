package korn03.tradeguardserver.scheduler;

import jakarta.annotation.PostConstruct;
import korn03.tradeguardserver.db.entity.Role;
import korn03.tradeguardserver.db.entity.User;
import korn03.tradeguardserver.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
public class UsersScheduler {

    private final Logger logger = LoggerFactory.getLogger(UsersScheduler.class);

    private final UserService userService;
    @Value("${tradeguard.default.user.username}")
    private String userUsername;
    @Value("${tradeguard.default.user.password}")
    private String userPassword;
    @Value("${tradeguard.default.admin.username}")
    private String adminUsername;
    @Value("${tradeguard.default.admin.password}")
    private String adminPassword;

    public UsersScheduler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Initializing default users
     */
    @PostConstruct
    //@EventListener(ApplicationReadyEvent.class)
    public void initDefaultUser() {
        if (!userService.userExists(userUsername)) {
            User user = new User();
            user.setUsername(userUsername);
            user.setPassword(userPassword);
            user.setRoles(Set.of(Role.USER));
            user.setRegisteredAt(Instant.now());
            userService.createUser(user);
        }
        if (!userService.userExists(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(adminPassword);
            admin.setRoles(Set.of(Role.USER, Role.ADMIN));
            admin.setRegisteredAt(Instant.now());
            userService.createUser(admin);
        }
        logger.info("DEFAULT USERS CREATED");
    }
}
