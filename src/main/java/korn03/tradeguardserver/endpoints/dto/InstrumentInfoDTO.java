package korn03.tradeguardserver.endpoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentInfoDTO {
    private int priceScale;
    private BigDecimal quantityStep;
    private long timestamp;
}
