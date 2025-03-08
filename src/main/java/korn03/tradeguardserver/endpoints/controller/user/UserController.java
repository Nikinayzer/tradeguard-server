package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.endpoints.dto.user.*;
import korn03.tradeguardserver.model.entity.Role;
import korn03.tradeguardserver.model.entity.User;
import korn03.tradeguardserver.model.entity.UserBybitAccount;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import korn03.tradeguardserver.service.user.UserBybitAccountService;
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
    private final UserBybitAccountService userBybitAccountService;

    public UserController(UserService userService, UserAccountLimitsService userAccountLimitsService, UserBybitAccountService userBybitAccountService) {
        this.userService = userService;
        this.userAccountLimitsService = userAccountLimitsService;
        this.userBybitAccountService = userBybitAccountService;
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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.loadUserById(#id).username == authentication.principal.username")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.loadUserById(id.intValue());
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
        User user = userService.loadUserById(id.intValue());

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

    @PostMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> addUserRole(@PathVariable Long id, @PathVariable Role role) {
        User user = userService.loadUserById(id.intValue());
        userService.addUserRole(user.getUsername(), role);
        return ResponseEntity.ok(convertToDTO(user));
    }

    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> removeUserRole(@PathVariable Long id, @PathVariable Role role) {
        User user = userService.loadUserById(id.intValue());
        userService.removeUserRole(user.getUsername(), role);
        return ResponseEntity.ok(convertToDTO(user));
    }

    // User Account Limits endpoints
    @GetMapping("/me/limits")
    public ResponseEntity<UserAccountLimitsDTO> getCurrentUserLimits() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return userAccountLimitsService.getUserLimits(user.getId()).map(this::convertToLimitsDTO).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/limits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountLimitsDTO> getUserLimits(@PathVariable Long id) {
        return userAccountLimitsService.getUserLimits(id).map(this::convertToLimitsDTO).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/limits")
    public ResponseEntity<UserAccountLimitsDTO> updateCurrentUserLimits(@RequestBody UserAccountLimitsRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        var limits = userAccountLimitsService.updateUserLimits(user.getId(), request.getDailyTradingLimit(), request.getMaximumLeverage(), request.getTradingCooldown(), request.getDailyLossLimit());
        return ResponseEntity.ok(convertToLimitsDTO(limits));
    }

    @PutMapping("/{id}/limits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountLimitsDTO> updateUserLimits(@PathVariable Long id, @RequestBody UserAccountLimitsRequestDTO request) {
        var limits = userAccountLimitsService.updateUserLimits(id, request.getDailyTradingLimit(), request.getMaximumLeverage(), request.getTradingCooldown(), request.getDailyLossLimit());
        return ResponseEntity.ok(convertToLimitsDTO(limits));
    }

    // Bybit Account endpoints
    @GetMapping("/me/bybit-accounts")
    public ResponseEntity<List<BybitAccountDTO>> getCurrentUserBybitAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        List<BybitAccountDTO> accounts = userBybitAccountService.getUserBybitAccounts(user.getId()).stream().map(this::convertToBybitAccountDTO).collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{userId}/bybit-accounts")
    @PreAuthorize("hasRole('ADMIN') or @userService.loadUserById(#userId).username == authentication.principal.username")
    public ResponseEntity<List<BybitAccountDTO>> getUserBybitAccounts(@PathVariable Long userId) {
        List<BybitAccountDTO> accounts = userBybitAccountService.getUserBybitAccounts(userId).stream().map(this::convertToBybitAccountDTO).collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/me/bybit-accounts")
    public ResponseEntity<BybitAccountDTO> createCurrentUserBybitAccount(@RequestBody BybitAccountRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        UserBybitAccount account = userBybitAccountService.saveBybitAccount(user.getId(), request.getName(), request.getReadOnlyApiKey(), request.getReadOnlyApiSecret(), request.getReadWriteApiKey(), request.getReadWriteApiSecret());
        return ResponseEntity.ok(convertToBybitAccountDTO(account));
    }

    @PostMapping("/{userId}/bybit-accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BybitAccountDTO> createUserBybitAccount(@PathVariable Long userId, @RequestBody BybitAccountRequestDTO request) {
        UserBybitAccount account = userBybitAccountService.saveBybitAccount(userId, request.getName(), request.getReadOnlyApiKey(), request.getReadOnlyApiSecret(), request.getReadWriteApiKey(), request.getReadWriteApiSecret());
        return ResponseEntity.ok(convertToBybitAccountDTO(account));
    }

    @DeleteMapping("/me/bybit-accounts/{id}")
    public ResponseEntity<Void> deleteCurrentUserBybitAccount(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        userBybitAccountService.deleteBybitAccount(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/bybit-accounts/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.loadUserById(#userId).username == authentication.principal.username")
    public ResponseEntity<Void> deleteUserBybitAccount(@PathVariable Long userId, @PathVariable Long id) {
        userBybitAccountService.deleteBybitAccount(userId, id);
        return ResponseEntity.ok().build();
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).firstName(user.getFirstName()).lastName(user.getLastName()).registeredAt(user.getRegisteredAt()).updatedAt(user.getUpdatedAt()).roles(user.getRoles()).accountNonExpired(user.isAccountNonExpired()).accountNonLocked(user.isAccountNonLocked()).credentialsNonExpired(user.isCredentialsNonExpired()).enabled(user.isEnabled()).build();
    }

    private UserAccountLimitsDTO convertToLimitsDTO(korn03.tradeguardserver.model.entity.UserAccountLimits limits) {
        return UserAccountLimitsDTO.builder().id(limits.getId()).userId(limits.getUser().getId()).dailyTradingLimit(limits.getDailyTradingLimit()).maximumLeverage(limits.getMaximumLeverage()).tradingCooldown(limits.getTradingCooldown()).dailyLossLimit(limits.getDailyLossLimit()).build();
    }

    private BybitAccountDTO convertToBybitAccountDTO(UserBybitAccount account) {
        return BybitAccountDTO.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .name(account.getAccountName())
                .readOnlyApiKey(maskApiKey(() -> userBybitAccountService.getDecryptedReadOnlyApiKey(account)))
                .readOnlyApiSecret(maskApiKey(() -> userBybitAccountService.getDecryptedReadOnlyApiSecret(account)))
                .readWriteApiKey(maskApiKey(() -> userBybitAccountService.getDecryptedReadWriteApiKey(account)))
                .readWriteApiSecret(maskApiKey(() -> userBybitAccountService.getDecryptedReadWriteApiSecret(account)))
                .build();
    }

    private String maskApiKey(Supplier<String> keySupplier) {
        String key = keySupplier.get();
        return userBybitAccountService.getMaskedToken(key);
    }
} 