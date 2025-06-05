package korn03.tradeguardserver.endpoints.controller.auth;

import korn03.tradeguardserver.endpoints.dto.auth.AuthResponseDTO;
import korn03.tradeguardserver.endpoints.dto.auth.oauth.DiscordExchangeRequestDTO;
import korn03.tradeguardserver.endpoints.dto.auth.oauth.DiscordTokenResponseDTO;
import korn03.tradeguardserver.endpoints.dto.auth.oauth.DiscordUserDTO;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.connections.UserDiscordAccount;
import korn03.tradeguardserver.security.AuthUtil;
import korn03.tradeguardserver.security.JwtService;
import korn03.tradeguardserver.service.core.pushNotifications.PushTokenService;
import korn03.tradeguardserver.service.user.UserService;
import korn03.tradeguardserver.service.user.connection.UserDiscordAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

//todo refactor
@RestController
@RequestMapping("/auth")
@Slf4j
public class DiscordAuthController {

    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserDiscordAccountService userDiscordAccountService;
    private final PushTokenService pushTokenService;

    @Value("${discord.client.id}")
    private String discordClientId;

    @Value("${discord.client.secret}")
    private String discordClientSecret;

    @Value("${discord.redirect.uri}")
    private String discordRedirectUri;

    public DiscordAuthController(RestTemplate restTemplate,
                                 JwtService jwtService,
                                 UserService userService,
                                 UserDiscordAccountService userDiscordAccountService, PushTokenService pushTokenService) {
        this.restTemplate = restTemplate;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userDiscordAccountService = userDiscordAccountService;
        this.pushTokenService = pushTokenService;
    }

    @PostMapping("/discord/exchange")
    public ResponseEntity<?> discordExchange(
            @RequestBody DiscordExchangeRequestDTO callbackRequest,
            @RequestHeader(value = "X-Push-Token", required = false) String pushToken
    ) {
        String code = callbackRequest.getCode();
        log.info("Authorizing Discord user with code: {}", code);
        // 1. Exchange the code for a Discord access token.
        DiscordTokenResponseDTO discordToken = exchangeCodeForToken(code, discordRedirectUri, callbackRequest.getCodeVerifier());
        if (discordToken == null) {
            log.error("Failed to exchange code for token: {}", code);  //todo
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token exchange with Discord.");
        }

        // 2. Retrieve Discord user info.
        DiscordUserDTO discordUserDTO = fetchDiscordUser(discordToken.getAccessToken());
        if (discordUserDTO == null) {
            log.error("Failed to fetch user info from Discord with token: {}", discordToken.getAccessToken()); //todo
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Failed to fetch data from Discord.");
        }

        // 3. Check if the account already exists in our system.
        User user;
        if (AuthUtil.isAuthenticated()) {
            user = AuthUtil.getCurrentUser();
            linkDiscordAccountToExistingUser(user, discordUserDTO);
        } else {
            user = handleUserAccount(discordUserDTO);
        }
        if (user == null) {
            log.error("Failed to handle user account for Discord ID: {}", discordUserDTO.getId()); //todo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during user account creation.");
        }
        if (pushToken != null) {
            pushTokenService.registerPushToken(user.getId(), pushToken);
        }

        // 4. Generate a JWT token and return auth response.
        String token = jwtService.generateToken(user);
        AuthResponseDTO response = AuthResponseDTO.builder()
                .token(token)
                .expiration(jwtService.extractExpiration(token).toString())
                .user(AuthResponseDTO.UserDTO.builder()
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .email(user.getEmail())
                        .build())
                .build();
        return ResponseEntity.ok(response);
    }

    private DiscordTokenResponseDTO exchangeCodeForToken(String code, String redirectUri, String codeVerifier) {
        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("client_id", discordClientId);
            requestBody.add("client_secret", discordClientSecret);
            requestBody.add("grant_type", "authorization_code");
            requestBody.add("code", code);
            requestBody.add("redirect_uri", redirectUri);
            requestBody.add("code_verifier", codeVerifier);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("Sending token exchange request to Discord...");

            ResponseEntity<DiscordTokenResponseDTO> response = restTemplate.postForEntity(
                    "https://discord.com/api/oauth2/token",
                    requestEntity,
                    DiscordTokenResponseDTO.class
            );

            log.info("Discord token exchange response status: {}", response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Token exchange failed. Response: {}", response);
                return null;
            } else {
                response.getBody();
            }

            return response.getBody();
        } catch (Exception ex) {
            log.error("Exception during token exchange with Discord", ex);
            return null;
        }
    }

    private DiscordUserDTO fetchDiscordUser(String accessToken) {
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<String> userRequestEntity = new HttpEntity<>(userHeaders);

        ResponseEntity<DiscordUserDTO> response = restTemplate.exchange(
                "https://discord.com/api/users/@me",
                HttpMethod.GET,
                userRequestEntity,
                DiscordUserDTO.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return null;
        } else {
            response.getBody();
        }
        return response.getBody();
    }

    /**
     * Retrieves the user by Discord ID or creates a new user if not found.
     */
    private User handleUserAccount(DiscordUserDTO discordUserDTO) {
        Long discordId;
        try {
            discordId = Long.parseLong(discordUserDTO.getId());
        } catch (NumberFormatException e) {
            return null;
        }
        log.info("Handling user account for Discord ID: {}", discordId);
        // Check if a Discord account already exists
        Optional<UserDiscordAccount> existingAccount = userDiscordAccountService.findByDiscordId(discordId);
        if (existingAccount.isPresent()) {
            userDiscordAccountService.updateDiscordAccount(
                    existingAccount.get().getUserId(),
                    discordId,
                    discordUserDTO.getUsername(),
                    discordUserDTO.getAvatar()
            );
            return userService.getById(existingAccount.get().getUserId());
        }

        // Try to find a user by email
        Optional<User> optionalUser = userService.getByEmail(discordUserDTO.getEmail());
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = new User();
            user.setIsExternal(true);
            user.setEmail(discordUserDTO.getEmail());
            user.setUsername(discordUserDTO.getUsername() + "#" + discordUserDTO.getDiscriminator());
            try {
                user = userService.createUser(user);
            } catch (Exception e) {
                log.error("Failed to create user: {}", e.getMessage());
                return null;
            }
        }
        // Save the Discord account linkage
        userDiscordAccountService.addDiscordAccount(
                user.getId(),
                discordId,
                discordUserDTO.getUsername(),
                discordUserDTO.getAvatar()
        );

        return user;
    }

    private void linkDiscordAccountToExistingUser(User user, DiscordUserDTO discord) {
        log.info("Linking Discord account to existing user: {}", user.getUsername());
        //if discord already exists, return
        if (userDiscordAccountService.findByDiscordId(Long.valueOf(discord.getId())).isPresent()) {
            throw new RuntimeException("Discord account already linked to another user");
        }
        userDiscordAccountService.addDiscordAccount(
                user.getId(),
                Long.valueOf(discord.getId()),
                discord.getUsername(),
                discord.getAvatar()
        );
        log.info("Linked Discord account to user: {}", user.getUsername());
    }
}



