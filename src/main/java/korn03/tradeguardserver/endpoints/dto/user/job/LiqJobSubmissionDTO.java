package korn03.tradeguardserver.endpoints.dto.user.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.*;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LiqJobSubmissionDTO extends JobSubmissionDTO {
    private List<String> excludeCoins;

    @NotNull(message = "Proportion percentage must be specified")
    @DecimalMin(value = "0.0", message = "Proportion percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Proportion percentage cannot exceed 100")
    private Double proportionPct;
} 