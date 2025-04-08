package korn03.tradeguardserver.endpoints.controller;

import korn03.tradeguardserver.endpoints.dto.auth.AuthRequestDTO;
import korn03.tradeguardserver.endpoints.dto.auth.AuthResponseDTO;
import korn03.tradeguardserver.endpoints.dto.auth.RegisterRequestDTO;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.security.JwtService;
import korn03.tradeguardserver.service.core.pushNotifications.PushTokenService;
import korn03.tradeguardserver.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

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
        user.setPassword(request.getPassword()); //todo hash
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User createdUser = userService.createUser(user);

        String token = jwtService.generateToken(createdUser);
        return ResponseEntity.ok(new AuthResponseDTO(token));
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

        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken
    ) {
        SecurityContextHolder.clearContext();
        if (pushToken != null) {
            pushTokenService.unregisterPushToken(pushToken);
        }
        return ResponseEntity.ok().build();
    }
}
