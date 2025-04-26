package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.endpoints.dto.user.exchangeAccount.ExchangeAccountCreationRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.exchangeAccount.ExchangeAccountDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UpdateUserAccountLimitsRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserUpdateRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.exchangeAccount.ExchangeAccountUpdateDTO;
import korn03.tradeguardserver.mapper.UserAccountLimitsMapper;
import korn03.tradeguardserver.model.entity.service.PushToken;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider;
import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.service.core.pushNotifications.PushNotificationService;
import korn03.tradeguardserver.service.core.pushNotifications.PushTokenService;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import korn03.tradeguardserver.service.user.UserService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import korn03.tradeguardserver.service.user.connection.UserExchangeAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
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

    //todo move this into service
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // Get optional Discord account
        Optional<UserDiscordAccount> discord = userDiscordAccountService.getDiscordAccount(user.getId());

        // Get list of Exchange accounts
        List<ExchangeAccountDTO> exchanges = userExchangeAccountService.getUserExchangeAccounts(user.getId());

        // Build UserDTO
        UserDTO.UserDTOBuilder userDTOBuilder = UserDTO.builder()
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
                .exchangeAccounts(exchanges);

        // Conditionally add discordAccount if Discord account exists
        discord.ifPresent(d -> userDTOBuilder.discordAccount(UserDTO.DiscordAccountDTO.builder()
                .discordId(String.valueOf(d.getDiscordId()))
                .username(d.getDiscordUsername())
                .avatar(d.getDiscordAvatar())
                .build()));

        // Build the final UserDTO object
        UserDTO userDTO = userDTOBuilder.build();

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
    public ResponseEntity<List<ExchangeAccountDTO>> getExchangeAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        List<ExchangeAccountDTO> accounts = userExchangeAccountService.getUserExchangeAccounts(user.getId());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/me/exchange-accounts/{id}")
    public ResponseEntity<ExchangeAccountDTO> getExchangeAccount(@PathVariable Long id) {
        Long userId = AuthUtil.getCurrentUserId();
        UserExchangeAccount account = userExchangeAccountService.getExchangeAccount(userId, id);
        return ResponseEntity.ok(convertToExchangeAccountDTO(account));
    }

    @PostMapping("/me/exchange-accounts/{id}/update")
    public ResponseEntity<ExchangeAccountDTO> updateExchangeAccount(@PathVariable Long id, @RequestBody ExchangeAccountUpdateDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        UserExchangeAccount account = userExchangeAccountService.updateExchangeAccount(
                user.getId(),
                id,
                request.getName(),
                request.getReadOnlyApiKey(),
                request.getReadOnlyApiSecret(),
                request.getReadWriteApiKey(),
                request.getReadWriteApiSecret()
        );
        return ResponseEntity.ok(convertToExchangeAccountDTO(account));
    }

    @PostMapping("/me/exchange-accounts/add")
    public ResponseEntity<ExchangeAccountDTO> createExchangeAccount(@RequestBody ExchangeAccountCreationRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String provider = request.getProvider().toUpperCase(Locale.ROOT);
        ExchangeProvider exchangeProvider = switch (provider) {
            case "BINANCE" -> ExchangeProvider.BINANCE_LIVE;
            case "BYBIT" -> Boolean.TRUE.equals(request.getDemo()) ? ExchangeProvider.BYBIT_DEMO :
                ExchangeProvider.BYBIT_LIVE;
            default -> throw new IllegalStateException("Unexpected value: " + provider);
        };
        UserExchangeAccount account = userExchangeAccountService.saveExchangeAccount(
                user.getId(),
                request.getName(),
                exchangeProvider,
                request.getReadOnlyApiKey(),
                request.getReadOnlyApiSecret(),
                request.getReadWriteApiKey(),
                request.getReadWriteApiSecret()
        );
        return ResponseEntity.ok(convertToExchangeAccountDTO(account));
    }

    @DeleteMapping("/me/exchange-accounts/{id}/delete")
    public ResponseEntity<ExchangeAccountDTO> deleteCurrentUserExchangeAccount(
            @PathVariable Long id
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        userExchangeAccountService.deleteExchangeAccount(user.getId(), id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{userId}/limits")
    //todo authorize!!!
    public ResponseEntity<UserAccountLimitsDTO> getUserLimits(@PathVariable Long userId) {
        if (userId == 493077349684740097L) { //TODO CHANGE BACK SINCE ITS DEV LOGIC TO TEST HEALTH
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
     *
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

    //todo make normal mapper
    private ExchangeAccountDTO convertToExchangeAccountDTO(UserExchangeAccount account) {
        return ExchangeAccountDTO.builder()
                .id(account.getId())
//                .userId(account.getUserId())
                .name(account.getAccountName())
                .provider(String.valueOf(account.getProvider()))
                .readOnlyApiKey(userExchangeAccountService.getDecryptedReadOnlyApiKey(account))
                .readOnlyApiSecret(userExchangeAccountService.getDecryptedReadOnlyApiSecret(account))
                .readWriteApiKey(userExchangeAccountService.getDecryptedReadWriteApiKey(account))
                .readWriteApiSecret(userExchangeAccountService.getDecryptedReadWriteApiSecret(account))
                .build();
    }

    private String maskApiKey(Supplier<String> keySupplier) {
        String key = keySupplier.get();
        return userExchangeAccountService.getMaskedToken(key);
    }
}