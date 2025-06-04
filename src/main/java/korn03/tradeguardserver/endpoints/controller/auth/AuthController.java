package korn03.tradeguardserver.endpoints.controller.auth;

import korn03.tradeguardserver.endpoints.dto.auth.AuthRequestDTO;
import korn03.tradeguardserver.endpoints.dto.auth.AuthResponseDTO;
import korn03.tradeguardserver.endpoints.dto.auth.RegisterRequestDTO;
import korn03.tradeguardserver.endpoints.dto.auth.twofactor.OtpVerificationRequestDTO;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.security.JwtService;
import korn03.tradeguardserver.service.auth.OtpContext;
import korn03.tradeguardserver.service.auth.OtpService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
//TODO refactor to authService
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final OtpService otpService;
    private final PushTokenService pushTokenService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, OtpService otpService, PushTokenService pushTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.otpService = otpService;
        this.pushTokenService = pushTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
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
        AuthResponseDTO response = AuthResponseDTO.builder().token(token).user(AuthResponseDTO.UserDTO.builder().username(createdUser.getUsername()).firstName(createdUser.getFirstName()).email(createdUser.getEmail()).build()).build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody AuthRequestDTO request,
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken
    ) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.clearContext();
        User user = (User) authentication.getPrincipal();

        if (!user.isTwoFactorEnabled()) {
            return buildAuthResponse(user, false, pushToken);
        }

        otpService.sendOtp(user, OtpContext.LOGIN);
        return buildAuthResponse(user, true, null);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOtp(
            @RequestBody OtpVerificationRequestDTO request,
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken) {
        if (!otpService.verifyOtp(request.getEmail(), request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        User user = userService.getByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return buildAuthResponse(user, false, pushToken);
    }

    private ResponseEntity<AuthResponseDTO> buildAuthResponse(User user, boolean twoFactorRequired, String pushToken) {
        String token = jwtService.generateToken(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (pushToken != null) {
            pushTokenService.registerPushToken(user.getId(), pushToken);
        }

        return ResponseEntity.ok(AuthResponseDTO.builder().twoFactorRequired(twoFactorRequired).token(twoFactorRequired ? null : token).user(convertUser(user)).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "X-Push-Token", required = false) String pushToken) {
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
    public ResponseEntity<Void> validateToken() {
        return AuthUtil.isAuthenticated() ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private AuthResponseDTO.UserDTO convertUser(User user) {
        return AuthResponseDTO.UserDTO.builder().username(user.getUsername()).firstName(user.getFirstName()).email(user.getEmail()).build();
    }
}
