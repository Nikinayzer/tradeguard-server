package korn03.tradeguardserver.endpoints.controller.auth;

import korn03.tradeguardserver.endpoints.dto.auth.AuthRequestDTO;
import korn03.tradeguardserver.endpoints.dto.auth.AuthResponseDTO;
import korn03.tradeguardserver.endpoints.dto.auth.RegisterRequestDTO;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.security.JwtService;
import korn03.tradeguardserver.service.core.pushNotifications.PushTokenService;
import korn03.tradeguardserver.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PushTokenService pushTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, PushTokenService pushTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.pushTokenService = pushTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @RequestBody RegisterRequestDTO request
    ) {
        if (userService.userExists(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User createdUser = userService.createUser(user);

        String token = jwtService.generateToken(createdUser);
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .user(AuthResponseDTO.UserDTO.builder()
                        .username(createdUser.getUsername())
                        .firstName(createdUser.getFirstName())
                        .email(createdUser.getEmail())
                        .build())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody AuthRequestDTO request,
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken
    ) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        if (pushToken != null) {
            pushTokenService.registerPushToken(user.getId(), pushToken);
        }
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .user(AuthResponseDTO.UserDTO.builder()
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .email(user.getEmail())
                        .build())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken
    ) {
        SecurityContextHolder.clearContext();
        if (pushToken != null) {
            try {
                pushTokenService.unregisterPushToken(pushToken);
            } catch (Exception e) {
                log.error("Failed to unregister push token: {}", pushToken, e);
            }

        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(
    ) {
        return AuthUtil.isAuthenticated()
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
