package korn03.tradeguardserver.security;

import korn03.tradeguardserver.exception.UnauthorizedException;
import korn03.tradeguardserver.model.entity.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthUtil {

    private static final String CODE_VERIFIER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
    private static final int CODE_VERIFIER_LENGTH = 128;

    private AuthUtil() {
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user;
            }
        }

        throw new UnauthorizedException("No jwt token found in the request headers.");
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User;
    }

    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder codeVerifier = new StringBuilder();
        for (int i = 0; i < CODE_VERIFIER_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(CODE_VERIFIER_CHARS.length());
            codeVerifier.append(CODE_VERIFIER_CHARS.charAt(randomIndex));
        }
        return codeVerifier.toString();
    }

    public static String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }
}
