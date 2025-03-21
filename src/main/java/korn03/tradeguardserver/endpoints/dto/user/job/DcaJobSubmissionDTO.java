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
@AllArgsConstructor
public class DcaJobSubmissionDTO extends JobSubmissionDTO {
    @NotNull(message = "Total amount must be specified")
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    private Double totalAmt;
} 