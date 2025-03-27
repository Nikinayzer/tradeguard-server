package korn03.tradeguardserver.endpoints.dto.user.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.*;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JobSubmissionDTO {

    @NotNull(message = "Name must be specified")
    private String name;

    @NotEmpty(message = "Coins list cannot be empty")
    private List<String> coins;

    @Pattern(regexp = "^(BUY|SELL|BOTH)$", message = "Side must be BUY, SELL or BOTH")
    private String side;

    @DecimalMin(value = "0.0", message = "Randomness percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Randomness percentage cannot exceed 100")
    private Double randomnessPct;

    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100")
    private Double discountPct;

    @NotNull(message = "Amount must be specified")
    @DecimalMin(value = "0.0", message = "Amount must be non-negative")
    private Double amount;

    @NotNull(message = "Total steps must be specified")
    @Min(value = 1, message = "Total steps must be at least 1")
    private Integer totalSteps;

    @NotNull(message = "Duration must be specified")
    @DecimalMin(value = "1.0", message = "Duration must be at least 1 minute")
    private Double durationMinutes;

    @NotBlank(message = "Source cannot be empty")
    private String source;
}
