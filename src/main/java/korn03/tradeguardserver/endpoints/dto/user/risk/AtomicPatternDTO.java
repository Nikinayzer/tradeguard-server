package korn03.tradeguardserver.endpoints.dto.user.risk;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AtomicPatternDTO extends BasePatternDTO {
    private Double severity;
    private Boolean consumed;
} 