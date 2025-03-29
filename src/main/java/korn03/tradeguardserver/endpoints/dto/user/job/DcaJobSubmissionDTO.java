package korn03.tradeguardserver.endpoints.dto.user.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class DcaJobSubmissionDTO extends JobSubmissionDTO {
    @NotNull(message = "Amount must be specified")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private Double amount;
} 