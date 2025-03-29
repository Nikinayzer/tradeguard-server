package korn03.tradeguardserver.endpoints.dto.user.job;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class LiqJobSubmissionDTO extends JobSubmissionDTO {
    @NotNull(message = "Amount must be specified")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    @DecimalMax(value = "100.0", message = "Amount cannot exceed 100")
    private Double amount;
} 