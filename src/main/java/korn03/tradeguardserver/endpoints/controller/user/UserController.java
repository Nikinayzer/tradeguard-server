package korn03.tradeguardserver.endpoints.controller.user;

import korn03.tradeguardserver.endpoints.dto.user.*;
import korn03.tradeguardserver.model.entity.Role;
import korn03.tradeguardserver.model.entity.User;
import korn03.tradeguardserver.service.user.UserAccountLimitsService;
import korn03.tradeguardserver.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserAccountLimitsService userAccountLimitsService;

    public UserController(UserService userService, UserAccountLimitsService userAccountLimitsService) {
        this.userService = userService;
        this.userAccountLimitsService = userAccountLimitsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(convertToDTO(createdUser));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@RequestBody UserUpdateRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getNewPassword() != null && request.getOldPassword() != null) {
            userService.changeUserPassword(user.getId(), request.getOldPassword(), request.getNewPassword());
        }
        
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequestDTO request) {
        User user = userService.loadUserById(id.intValue());
        
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getNewPassword() != null && request.getOldPassword() != null) {
            userService.changeUserPassword(id, request.getOldPassword(), request.getNewPassword());
        }
        
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
        return userAccountLimitsService.getUserLimits(user.getId())
                .map(this::convertToLimitsDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/limits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountLimitsDTO> getUserLimits(@PathVariable Long id) {
        return userAccountLimitsService.getUserLimits(id)
                .map(this::convertToLimitsDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me/limits")
    public ResponseEntity<UserAccountLimitsDTO> updateCurrentUserLimits(@RequestBody UserAccountLimitsRequestDTO request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        var limits = userAccountLimitsService.updateUserLimits(
            user.getId(),
            request.getDailyTradingLimit(),
            request.getMaximumLeverage(),
            request.getTradingCooldown(),
            request.getDailyLossLimit()
        );
        return ResponseEntity.ok(convertToLimitsDTO(limits));
    }

    @PutMapping("/{id}/limits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccountLimitsDTO> updateUserLimits(
            @PathVariable Long id,
            @RequestBody UserAccountLimitsRequestDTO request) {
        var limits = userAccountLimitsService.updateUserLimits(
            id,
            request.getDailyTradingLimit(),
            request.getMaximumLeverage(),
            request.getTradingCooldown(),
            request.getDailyLossLimit()
        );
        return ResponseEntity.ok(convertToLimitsDTO(limits));
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
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
                .build();
    }

    private UserAccountLimitsDTO convertToLimitsDTO(korn03.tradeguardserver.model.entity.UserAccountLimits limits) {
        return UserAccountLimitsDTO.builder()
                .id(limits.getId())
                .userId(limits.getUser().getId())
                .dailyTradingLimit(limits.getDailyTradingLimit())
                .maximumLeverage(limits.getMaximumLeverage())
                .tradingCooldown(limits.getTradingCooldown())
                .dailyLossLimit(limits.getDailyLossLimit())
                .build();
    }
} 