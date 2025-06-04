package korn03.tradeguardserver.service.user;

import jakarta.ws.rs.NotFoundException;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UpdateUserAccountLimitsRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.mapper.UserAccountLimitsMapper;
import korn03.tradeguardserver.model.entity.user.User;
import korn03.tradeguardserver.model.entity.user.UserAccountLimits;
import korn03.tradeguardserver.model.repository.user.UserAccountLimitsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserAccountLimitsService {

    private final UserAccountLimitsRepository userAccountLimitsRepository;
    private final UserAccountLimitsMapper userAccountLimitsMapper;

    public UserAccountLimitsService(
            UserAccountLimitsRepository userAccountLimitsRepository,
            UserAccountLimitsMapper userAccountLimitsMapper
    ) {
        this.userAccountLimitsRepository = userAccountLimitsRepository;
        this.userAccountLimitsMapper = userAccountLimitsMapper;
    }

    /**
     * Retrieves account limits for a specific user by userId.
     */
    public UserAccountLimitsDTO getLimitsByUserId(Long userId) {
        return userAccountLimitsRepository.findByUserId(userId)
                .map(userAccountLimitsMapper::toDTO)
                .orElseThrow(() -> new NotFoundException("UserAccountLimits not found for userId: " + userId));
    }

    /**
     * Updates user account limits.
     */
    @Transactional
    public UserAccountLimitsDTO updateUserLimits(Long userId, UpdateUserAccountLimitsRequestDTO newLimitsDTO) {
        UserAccountLimits existingLimits = userAccountLimitsRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("UserAccountLimits not found for userId: " + userId));

        userAccountLimitsMapper.updateEntityFromDTO(newLimitsDTO, existingLimits);

        return userAccountLimitsMapper.toDTO(userAccountLimitsRepository.save(existingLimits));
    }

    /**
     * Creates default limits for a new user.
     */
    public void createDefaultLimits(User user) {
        UserAccountLimits limits = UserAccountLimits.builder()
                .userId(user.getId())
                .maxSingleJobLimit(BigDecimal.valueOf(500))
                .maxDailyTradingLimit(BigDecimal.valueOf(1000))
                .maxConcurrentOrders(3)
                .maxDailyTrades(10)
                .tradingCooldown(2) // 2 minutes
                .maxPortfolioRisk(BigDecimal.valueOf(5.0)) // 5%
                .dailyLossLimit(BigDecimal.valueOf(5000))
                .maxConsecutiveLosses(3)
                .maxDailyBalanceChange(BigDecimal.valueOf(10.0))
//                .volatilityLimit(BigDecimal.valueOf(10.0))
//                .liquidityThreshold(BigDecimal.valueOf(1000000))
                .build();

        userAccountLimitsRepository.save(limits);
    }
}
