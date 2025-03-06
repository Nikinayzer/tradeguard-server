package korn03.tradeguardserver.endpoints.dto.bybit.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitMarketResponse<T> {
    private int retCode;
    private String retMsg;
    private BybitMarketResult<T> result;
    private List<String> retExtInfo; //todo check if really String
    private Instant time; //todo check if really Instant
}


