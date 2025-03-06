package korn03.tradeguardserver.endpoints.dto.bybit.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BybitMarketResult<T> {
    private String category; // "linear", "inverse", "option", "spot"
    private List<T> list;
}