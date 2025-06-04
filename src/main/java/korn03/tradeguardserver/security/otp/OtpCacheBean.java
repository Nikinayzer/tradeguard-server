package korn03.tradeguardserver.security.otp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class OtpCacheBean {

    @Value("${otp.expiration-minutes}")
    private Integer otpExpirationMinutes;

    @Bean
    public LoadingCache<String, Integer> loadingCache() {
        return CacheBuilder.newBuilder().expireAfterWrite(otpExpirationMinutes, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @NotNull
                    public Integer load(@NotNull String key) {
                        return 0;
                    }
                });
    }

    public Integer getOtpExpirationMinutes() {
        return otpExpirationMinutes;
    }

}