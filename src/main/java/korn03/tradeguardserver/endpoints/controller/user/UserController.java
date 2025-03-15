package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.endpoints.dto.user.*;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UpdateUserAccountLimitsRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.mapper.UserAccountLimitsMapper;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.UserBybitAccount;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import korn03.tradeguardserver.service.user.connection.UserBybitAccountService;
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
    private final UserAccountLimitsMapper userAccountLimitsMapper;

    public UserController(UserService userService, UserAccountLimitsService userAccountLimitsService, UserBybitAccountService userBybitAccountService, UserAccountLimitsMapper userAccountLimitsMapper) {
        this.userService = userService;
        this.userAccountLimitsService = userAccountLimitsService;
        this.userBybitAccountService = userBybitAccountService;
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

    @GetMapping("/me/bybit-accounts")
    public ResponseEntity<List<BybitAccountDTO>> getCurrentUserBybitAccounts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        List<BybitAccountDTO> accounts = userBybitAccountService.getUserBybitAccounts(user.getId()).stream().map(this::convertToBybitAccountDTO).collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{userId}/bybit-accounts")
    @PreAuthorize("hasRole('ADMIN') or @userService.userById(#userId).username == authentication.principal.username")
    public ResponseEntity<List<BybitAccountDTO>> getUserBybitAccounts(@PathVariable Long userId) {
        List<BybitAccountDTO> accounts = userBybitAccountService.getUserBybitAccounts(userId).stream().map(this::convertToBybitAccountDTO).collect(Collectors.toList());
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/me/bybit-accounts/add")
    public ResponseEntity<BybitAccountDTO> createCurrentUserBybitAccount(@RequestBody BybitAccountRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        UserBybitAccount account = userBybitAccountService.saveBybitAccount(user.getId(), request.getName(), request.getReadOnlyApiKey(), request.getReadOnlyApiSecret(), request.getReadWriteApiKey(), request.getReadWriteApiSecret());
        return ResponseEntity.ok(convertToBybitAccountDTO(account));
    }

    @PostMapping("/me/bybit-accounts/delete")
    public ResponseEntity<BybitAccountDTO> deleteCurrentUserBybitAccount(@RequestBody BybitAccountRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        userBybitAccountService.deleteBybitAccount(user.getId(), request.getId());
        return ResponseEntity.ok().build();
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
    @PreAuthorize("hasRole('ADMIN') or @userService.userById(#userId).username == authentication.principal.username")
    public ResponseEntity<Void> deleteUserBybitAccount(@PathVariable Long userId, @PathVariable Long id) {
        userBybitAccountService.deleteBybitAccount(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/limits")
    public ResponseEntity<UserAccountLimitsDTO> getCurrentUserLimits() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(userAccountLimitsService.getLimitsByUserId(user.getId()));
    }

    @PutMapping("/me/limits")
    public ResponseEntity<UserAccountLimitsDTO> updateCurrentUserLimits(@RequestBody UpdateUserAccountLimitsRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        userAccountLimitsService.updateUserLimits(user.getId(), request);
        return ResponseEntity.ok(userAccountLimitsService.updateUserLimits(user.getId(), request));
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder().id(user.getId()).username(user.getUsername()).email(user.getEmail()).firstName(user.getFirstName()).lastName(user.getLastName()).registeredAt(user.getRegisteredAt()).updatedAt(user.getUpdatedAt()).roles(user.getRoles()).accountNonExpired(user.isAccountNonExpired()).accountNonLocked(user.isAccountNonLocked()).credentialsNonExpired(user.isCredentialsNonExpired()).enabled(user.isEnabled()).build();
    }

    private BybitAccountDTO convertToBybitAccountDTO(UserBybitAccount account) {
        return BybitAccountDTO.builder().id(account.getId()).userId(account.getUserId()).name(account.getAccountName()).readOnlyApiKey(maskApiKey(() -> userBybitAccountService.getDecryptedReadOnlyApiKey(account))).readOnlyApiSecret(maskApiKey(() -> userBybitAccountService.getDecryptedReadOnlyApiSecret(account))).readWriteApiKey(maskApiKey(() -> userBybitAccountService.getDecryptedReadWriteApiKey(account))).readWriteApiSecret(maskApiKey(() -> userBybitAccountService.getDecryptedReadWriteApiSecret(account))).build();
    }

    private String maskApiKey(Supplier<String> keySupplier) {
        String key = keySupplier.get();
        return userBybitAccountService.getMaskedToken(key);
    }
}