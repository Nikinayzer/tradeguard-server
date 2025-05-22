package korn03.tradeguardserver.mapper;

import korn03.tradeguardserver.endpoints.dto.user.equity.EquityFrontendDTO;
import korn03.tradeguardserver.kafka.events.equity.EquityKafkaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EquityMapper {
    @Mapping(target = "balances.wallet", source = "equity.walletBalance")
    @Mapping(target = "balances.available", source = "equity.availableBalance")
    @Mapping(target = "balances.bnb", source = "equity.bnbBalanceUsdt")
    @Mapping(target = "totalUnrealizedPnl", source = "equity.totalUnrealizedPnl")
    EquityFrontendDTO toFrontendDTO(EquityKafkaDTO kafkaDTO);
    
    List<EquityFrontendDTO> toFrontendDTOList(List<EquityKafkaDTO> kafkaDTOs);
} 