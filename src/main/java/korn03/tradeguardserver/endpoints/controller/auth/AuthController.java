package korn03.tradeguardserver.endpoints.controller.auth;

import jakarta.validation.Valid;
import korn03.tradeguardserver.endpoints.dto.auth.AuthRequestDTO;
import korn03.tradeguardserver.endpoints.dto.auth.AuthResponseDTO;
import korn03.tradeguardserver.endpoints.dto.auth.twofactor.OtpVerificationRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserRegisterRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserUpdatePasswordRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserUpdatePasswordRequestVerifyDTO;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.security.JwtService;
import korn03.tradeguardserver.service.auth.OtpContext;
import korn03.tradeguardserver.service.auth.OtpService;
import korn03.tradeguardserver.service.core.pushNotifications.PushTokenService;
import korn03.tradeguardserver.service.email.EmailService;
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
    private final EmailService emailService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService, OtpService otpService, PushTokenService pushTokenService, EmailService emailService, EmailService emailService1) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.otpService = otpService;
        this.pushTokenService = pushTokenService;
        this.emailService = emailService1;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequestDTO request) {
        userService.createUserFromDTO(request);
        emailService.sendRegistrationEmail(request.getEmail(), request.getFirstName());
        return ResponseEntity.ok("all good");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody AuthRequestDTO request,
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken
    ) {
        if (request.getIdentifier().contains("@")) {
            User user = userService.getByEmailOrThrow(request.getIdentifier());
            request.setIdentifier(user.getUsername());
        }
        //todo something better
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));
        SecurityContextHolder.clearContext();
        User user = (User) authentication.getPrincipal();

        if (!user.isTwoFactorEnabled() && user.isEmailVerified()) {
            return buildAuthResponse(user, false, pushToken);
        }
        otpService.sendOtp(user.getEmail(), user.getFirstName(), OtpContext.LOGIN);
        return buildAuthResponse(user, true, null);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDTO> verifyOtp(
            @RequestBody OtpVerificationRequestDTO request,
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken) {
        if (!otpService.verifyOtp(request.getEmail(), request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }
        User user = userService.verifyUserEmail(request.getEmail());
        return buildAuthResponse(user, false, pushToken);
    }

    @PostMapping("/password-change")
    public ResponseEntity<?> changePassword(
            @RequestBody UserUpdatePasswordRequestDTO request
    ) {
        User user = userService.getByEmailOrThrow(request.getEmail());
        otpService.sendOtp(user.getEmail(), user.getFirstName(), OtpContext.PASSWORD_RESET);
        return ResponseEntity.ok("Verification request was sent to your email. Please check your inbox.");
    }

    @PostMapping("/password-change/verify")
    public ResponseEntity<?> verifyPasswordChange(
            @Valid @RequestBody UserUpdatePasswordRequestVerifyDTO request
    ) {
        String email = request.getEmail();
        User user = userService.getByEmailOrThrow(email);
        String otp = request.getCode();
        if (otpService.verifyOtp(email, otp)) {
            userService.changeUserPassword(user.getId(), request.getNewPassword());
            return buildAuthResponse(user, false, null);
        } else {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }
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

    private ResponseEntity<AuthResponseDTO> buildAuthResponse(User user, boolean twoFactorRequired, String pushToken) {
        String token = jwtService.generateToken(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (pushToken != null) {
            pushTokenService.registerPushToken(user.getId(), pushToken);
        }

        return ResponseEntity.ok(
                AuthResponseDTO.builder()
                        .twoFactorRequired(twoFactorRequired)
                        .token(twoFactorRequired ? null : token)
                        .expiration(jwtService.extractExpiration(token).toString())
                        .user(convertUser(user))
                        .build());
    }

    private AuthResponseDTO.UserDTO convertUser(User user) {
        return AuthResponseDTO.UserDTO.builder().username(user.getUsername()).firstName(user.getFirstName()).email(user.getEmail()).build();
    }
}
