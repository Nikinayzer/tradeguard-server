package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.endpoints.dto.user.*;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UpdateUserAccountLimitsRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.mapper.UserAccountLimitsMapper;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import korn03.tradeguardserver.service.user.connection.UserExchangeAccountService;
import korn03.tradeguardserver.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserAccountLimitsService userAccountLimitsService;
    private final UserExchangeAccountService userExchangeAccountService;
    private final UserAccountLimitsMapper userAccountLimitsMapper;

    public UserController(UserService userService, UserAccountLimitsService userAccountLimitsService, UserExchangeAccountService userExchangeAccountService, UserAccountLimitsMapper userAccountLimitsMapper) {
        this.userService = userService;
        this.userAccountLimitsService = userAccountLimitsService;
        this.userExchangeAccountService = userExchangeAccountService;
        this.userAccountLimitsMapper = userAccountLimitsMapper;
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
        return ResponseEntity.ok(convertToDTO(user));
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