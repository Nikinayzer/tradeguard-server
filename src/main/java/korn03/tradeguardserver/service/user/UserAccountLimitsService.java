package korn03.tradeguardserver.service.user;

import korn03.tradeguardserver.model.entity.User;
import korn03.tradeguardserver.model.entity.UserAccountLimits;
import korn03.tradeguardserver.model.repository.UserAccountLimitsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Service
public class UserAccountLimitsService {

    private final UserAccountLimitsRepository userAccountLimitsRepository;
    private final UserService userService;

    public UserAccountLimitsService(UserAccountLimitsRepository userAccountLimitsRepository, UserService userService) {
        this.userAccountLimitsRepository = userAccountLimitsRepository;
        this.userService = userService;
    }

    public UserAccountLimits createUserLimits(Long userId, BigDecimal dailyTradingLimit, BigDecimal maximumLeverage,
                                           Duration tradingCooldown, BigDecimal dailyLossLimit) {
        User user = userService.loadUserById(userId.intValue());
        
        UserAccountLimits limits = UserAccountLimits.builder()
                .user(user)
                .dailyTradingLimit(dailyTradingLimit)
                .maximumLeverage(maximumLeverage)
                .tradingCooldown(tradingCooldown)
                .dailyLossLimit(dailyLossLimit)
                .build();

        return userAccountLimitsRepository.save(limits);
    }

    public Optional<UserAccountLimits> getUserLimits(Long userId) {
        return userAccountLimitsRepository.findByUserId(userId);
    }

    public UserAccountLimits updateUserLimits(Long userId, BigDecimal dailyTradingLimit, BigDecimal maximumLeverage,
                                           Duration tradingCooldown, BigDecimal dailyLossLimit) {
        UserAccountLimits limits = userAccountLimitsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User limits not found"));

        limits.setDailyTradingLimit(dailyTradingLimit);
        limits.setMaximumLeverage(maximumLeverage);
        limits.setTradingCooldown(tradingCooldown);
        limits.setDailyLossLimit(dailyLossLimit);

        return userAccountLimitsRepository.save(limits);
    }

    public void deleteUserLimits(Long userId) {
        userAccountLimitsRepository.findByUserId(userId)
                .ifPresent(userAccountLimitsRepository::delete);
    }
} 