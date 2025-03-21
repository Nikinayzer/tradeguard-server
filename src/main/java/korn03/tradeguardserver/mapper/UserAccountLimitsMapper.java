package korn03.tradeguardserver.mapper;

import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UpdateUserAccountLimitsRequestDTO;
import korn03.tradeguardserver.endpoints.dto.user.UserAccountLimits.UserAccountLimitsDTO;
import korn03.tradeguardserver.model.entity.user.UserAccountLimits;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.Duration;

@Mapper(componentModel = "spring")
public interface UserAccountLimitsMapper {

    @Mapping(source = "id", target = "id")
    UserAccountLimitsDTO toDTO(UserAccountLimits entity);

    @Mapping(source = "id", target = "id")
    UserAccountLimits toEntity(UserAccountLimitsDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateEntityFromDTO(UserAccountLimitsDTO dto, @MappingTarget UserAccountLimits entity);

    @Mapping(source = "maxSingleJobLimit", target = "maxSingleJobLimit", qualifiedByName = "stringToBigDecimal")
    @Mapping(source = "maxDailyTradingLimit", target = "maxDailyTradingLimit", qualifiedByName = "stringToBigDecimal")
    @Mapping(source = "maxConcurrentOrders", target = "maxConcurrentOrders", qualifiedByName = "stringToInteger")
    @Mapping(source = "maxDailyTrades", target = "maxDailyTrades", qualifiedByName = "stringToInteger")
    @Mapping(source = "tradingCooldown", target = "tradingCooldown", qualifiedByName = "stringToInteger")
    @Mapping(source = "maxPortfolioRisk", target = "maxPortfolioRisk", qualifiedByName = "stringToBigDecimal")
    @Mapping(source = "allowDcaForce", target = "allowDcaForce", qualifiedByName = "stringToBoolean")
    @Mapping(source = "allowLiqForce", target = "allowLiqForce", qualifiedByName = "stringToBoolean")
    @Mapping(source = "dailyLossLimit", target = "dailyLossLimit", qualifiedByName = "stringToBigDecimal")
    @Mapping(source = "maxConsecutiveLosses", target = "maxConsecutiveLosses", qualifiedByName = "stringToInteger")
    @Mapping(source = "maxDailyBalanceChange", target = "maxDailyBalanceChange", qualifiedByName = "stringToBigDecimal")
    @Mapping(source = "volatilityLimit", target = "volatilityLimit", qualifiedByName = "stringToBigDecimal")
    @Mapping(source = "liquidityThreshold", target = "liquidityThreshold", qualifiedByName = "stringToBigDecimal")
    UserAccountLimits toEntity(UpdateUserAccountLimitsRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateEntityFromDTO(UpdateUserAccountLimitsRequestDTO dto, @MappingTarget UserAccountLimits entity);


    @Named("stringToBigDecimal")
    static BigDecimal stringToBigDecimal(String value) {
        return (value != null && !value.isEmpty()) ? new BigDecimal(value) : null;
    }

    @Named("stringToInteger")
    static Integer stringToInteger(String value) {
        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : null;
    }

    @Named("stringToBoolean")
    static Boolean stringToBoolean(String value) {
        return (value != null) ? Boolean.parseBoolean(value) : null;
    }

}
