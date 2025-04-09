package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.endpoints.dto.user.*;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UpdateUserAccountLimitsRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.mapper.UserAccountLimitsMapper;
import korn03.tradeguardserver.model.entity.service.PushToken;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.service.core.pushNotifications.PushNotificationService;
import korn03.tradeguardserver.service.core.pushNotifications.PushTokenService;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import korn03.tradeguardserver.service.user.connection.UserExchangeAccountService;
import korn03.tradeguardserver.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserAccountLimitsService userAccountLimitsService;
    private final UserExchangeAccountService userExchangeAccountService;
    private final UserAccountLimitsMapper userAccountLimitsMapper;
    private final PushTokenService pushTokenService;
    private final PushNotificationService pushNotificationService;
    private final UserDiscordAccountService userDiscordAccountService;

    public UserController(UserService userService, UserAccountLimitsService userAccountLimitsService, UserExchangeAccountService userExchangeAccountService, UserAccountLimitsMapper userAccountLimitsMapper, PushTokenService pushTokenService, PushNotificationService pushNotificationService, UserDiscordAccountService userDiscordAccountService) {
        this.userService = userService;
        this.userAccountLimitsService = userAccountLimitsService;
        this.userExchangeAccountService = userExchangeAccountService;
        this.userAccountLimitsMapper = userAccountLimitsMapper;
        this.pushTokenService = pushTokenService;
        this.pushNotificationService = pushNotificationService;
        this.userDiscordAccountService = userDiscordAccountService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        //TODO THIS IS HORRIBLE, REFACTOR
        Optional<UserDiscordAccount> discord = userDiscordAccountService.getDiscordAccount(user.getId());
        List<ExchangeAccountDTO>  exchanges = userExchangeAccountService.getUserExchangeAccounts(user.getId());
        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .registeredAt(user.getRegisteredAt())
                .updatedAt(user.getUpdatedAt())
                .roles(user.getRoles())
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .enabled(user.isEnabled())
                .discordAccount(UserDTO.DiscordAccountDTO.builder()
                        .discordId(String.valueOf(discord.map(UserDiscordAccount::getDiscordId).orElse(null)))
                        .username(discord.map(UserDiscordAccount::getDiscordUsername).orElse(null))
                        .discriminator(discord.map(UserDiscordAccount::getDiscordDiscriminator).orElse(null))
                        .avatar(discord.map(UserDiscordAccount::getDiscordAvatar).orElse(null))
                        .build())
                .exchangeAccounts(exchanges)
                .build();
        return ResponseEntity.ok(userDTO);
    }


    @PostMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@RequestBody UserUpdateRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        userService.updateUser(user);

        return ResponseEntity.ok(convertToDTO(user));
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequestDTO request) {
        User user = userService.getById(id);

        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());

        return ResponseEntity.ok(convertToDTO(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/exchange-accounts")
    public ResponseEntity<List<ExchangeAccountDTO>> getCurrentUserExchangeAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        List<ExchangeAccountDTO> accounts = userExchangeAccountService.getUserExchangeAccounts(user.getId());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{userId}/exchange-accounts")
    @PreAuthorize("hasRole('ADMIN') or @userService.userById(#userId).username == authentication.principal.username")
    public ResponseEntity<List<ExchangeAccountDTO>> getUserExchangeAccounts(@PathVariable Long userId) {
        List<ExchangeAccountDTO> accounts = userExchangeAccountService.getUserExchangeAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/me/exchange-accounts/add")
    //todo design either 2 endpoints for each provider or unified
    public ResponseEntity<ExchangeAccountDTO> createCurrentUserExchangeAccount(@RequestBody ExchangeAccountRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        UserExchangeAccount account = userExchangeAccountService.saveBybitAccount(user.getId(), request.getName(), request.getReadOnlyApiKey(), request.getReadOnlyApiSecret(), request.getReadWriteApiKey(), request.getReadWriteApiSecret());
        return ResponseEntity.ok(convertToExchangeAccountDTO(account));
    }

    @PostMapping("/me/exchange-accounts/delete")
    //todo design either 2 endpoints for each provider or unified
    public ResponseEntity<ExchangeAccountDTO> deleteCurrentUserExchangeAccount(@RequestBody ExchangeAccountRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        userExchangeAccountService.deleteExchangeAccount(user.getId(), request.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/exchange-accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ExchangeAccountDTO> createUserExchangeAccount(@PathVariable Long userId, @RequestBody ExchangeAccountRequestDTO request) {
        UserExchangeAccount account = userExchangeAccountService.saveBybitAccount(userId, request.getName(), request.getReadOnlyApiKey(), request.getReadOnlyApiSecret(), request.getReadWriteApiKey(), request.getReadWriteApiSecret());
        return ResponseEntity.ok(convertToExchangeAccountDTO(account));
    }

    @DeleteMapping("/me/exchange-accounts/{id}")
    public ResponseEntity<Void> deleteCurrentUserExchangeAccount(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        userExchangeAccountService.deleteExchangeAccount(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/exchange-accounts/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.userById(#userId).username == authentication.principal.username")
    public ResponseEntity<Void> deleteUserExchangeAccount(@PathVariable Long userId, @PathVariable Long id) {
        userExchangeAccountService.deleteExchangeAccount(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/limits")
    //todo authorize!!!
    public ResponseEntity<UserAccountLimitsDTO> getUserLimits(@PathVariable Long userId) {
        if (userId == 493077349684740097L){ //TODO CHANGE BACK SINCE ITS DEV LOGIC TO TEST HEALTH
            return ResponseEntity.ok(userAccountLimitsService.getLimitsByUserId(2L));
        }
        return ResponseEntity.ok(userAccountLimitsService.getLimitsByUserId(userId));
    }

    @GetMapping("/me/limits")
    public ResponseEntity<UserAccountLimitsDTO> getCurrentUserLimits() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userAccountLimitsService.getLimitsByUserId(user.getId()));
    }

    @PostMapping("/me/limits")
    public ResponseEntity<UserAccountLimitsDTO> updateCurrentUserLimits(@RequestBody UpdateUserAccountLimitsRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        userAccountLimitsService.updateUserLimits(user.getId(), request);
        return ResponseEntity.ok(userAccountLimitsService.updateUserLimits(user.getId(), request));
    }

    @GetMapping("/{userId}/pushTokens")
    public ResponseEntity<List<PushToken>> getUserPushTokens(@PathVariable Long userId) {
        List<PushToken> pushTokens = pushTokenService.getPushTokensByUserId(userId);
        return ResponseEntity.ok(pushTokens);
    }

    /**
     * Send push notification to user
     * Example: POST /users/{userId}/send?title=Hello&body=World
     * @param userId
     * @param title
     * @param body
     * @return
     */
    @PostMapping("/{userId}/sendPush")
    public ResponseEntity<Void> sendPushNotification(@PathVariable Long userId, @RequestParam String title, @RequestParam String body) {
        pushNotificationService.sendPushNotification(userId, title, body);
        return ResponseEntity.ok().build();
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).firstName(user.getFirstName()).lastName(user.getLastName()).registeredAt(user.getRegisteredAt()).updatedAt(user.getUpdatedAt()).roles(user.getRoles()).accountNonExpired(user.isAccountNonExpired()).accountNonLocked(user.isAccountNonLocked()).credentialsNonExpired(user.isCredentialsNonExpired()).enabled(user.isEnabled()).build();
    }

    private ExchangeAccountDTO convertToExchangeAccountDTO(UserExchangeAccount account) {
        return ExchangeAccountDTO.builder().id(account.getId()).userId(account.getUserId()).name(account.getAccountName()).readOnlyApiKey(maskApiKey(() -> userExchangeAccountService.getDecryptedReadOnlyApiKey(account))).readOnlyApiSecret(maskApiKey(() -> userExchangeAccountService.getDecryptedReadOnlyApiSecret(account))).readWriteApiKey(maskApiKey(() -> userExchangeAccountService.getDecryptedReadWriteApiKey(account))).readWriteApiSecret(maskApiKey(() -> userExchangeAccountService.getDecryptedReadWriteApiSecret(account))).build();
    }

    private String maskApiKey(Supplier<String> keySupplier) {
        String key = keySupplier.get();
        return userExchangeAccountService.getMaskedToken(key);
    }
}