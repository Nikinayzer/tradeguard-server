package korn03.tradeguardserver.service.auth;

import com.google.common.cache.LoadingCache;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.service.core.deeplink.DeeplinkBuilder;
import korn03.tradeguardserver.service.core.deeplink.DeeplinkRoute;
import korn03.tradeguardserver.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final LoadingCache<String, Integer> oneTimePasswordCache;
    private final EmailService emailService;
    private final DeeplinkBuilder deeplinkBuilder;

    @Value("${otp.expiration-minutes}")
    private Integer otpExpirationMinutes;

    public void sendOtp(User user, OtpContext context) { //context is important for other otp based actions, todo
        try {
            oneTimePasswordCache.invalidate(user.getEmail());
        } catch (Exception e) {
            log.error("FAILED TO INVALIDATE OTP CACHE: {}", e.getMessage());
        }

        SecureRandom random = new SecureRandom();
        int otp = 100_000 + random.nextInt(900_000);
        oneTimePasswordCache.put(user.getEmail(), otp);

        CompletableFuture.runAsync(() ->
                emailService.sendOtpTemplateEmail(
                        user.getEmail(),
                        user.getFirstName(),
                        otp,
                        otpExpirationMinutes,
                        deeplinkBuilder.build(DeeplinkRoute.OTP,
                                Map.of(
                                        "otp", String.valueOf(otp)
                                ))
                )
        );
    }

    public boolean verifyOtp(String email, String otp) {
        try {
            Integer cachedOtp = oneTimePasswordCache.get(email);
            int code = Integer.parseInt(otp);
            return cachedOtp.equals(code);
        } catch (ExecutionException e) {
            log.error("FAILED TO FETCH OTP FROM CACHE: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid One-Time Code");
        }
    }
}

