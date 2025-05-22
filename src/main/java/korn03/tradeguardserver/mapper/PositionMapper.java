package korn03.tradeguardserver.mapper;

import korn03.tradeguardserver.endpoints.dto.user.position.PositionFrontendDTO;
import korn03.tradeguardserver.kafka.events.position.PositionKafkaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PositionMapper {
    @Mapping(target = "size.quantity", source = "position.qty")
    @Mapping(target = "size.value", source = "position.usdtAmt")
    @Mapping(target = "prices.entry", source = "position.entryPrice")
    @Mapping(target = "prices.mark", source = "position.markPrice")
    @Mapping(target = "prices.liquidation", source = "position.liquidationPrice")
    @Mapping(target = "pnl.unrealized", source = "position.unrealizedPnl")
    @Mapping(target = "pnl.current", source = "position.curRealizedPnl")
    @Mapping(target = "pnl.cumulative", source = "position.cumRealizedPnl")
    @Mapping(target = "symbol", source = "position.symbol")
    @Mapping(target = "side", source = "position.side")
    @Mapping(target = "leverage", source = "position.leverage")
    PositionFrontendDTO toFrontendDTO(PositionKafkaDTO kafkaDTO);
    
    List<PositionFrontendDTO> toFrontendDTOList(List<PositionKafkaDTO> kafkaDTOs);
} 