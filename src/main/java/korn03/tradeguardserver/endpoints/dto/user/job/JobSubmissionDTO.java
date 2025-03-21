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
    @NotBlank(message = "Source cannot be empty")
    private String source;

    @NotNull(message = "Total steps must be specified")
    @Min(value = 1, message = "Total steps must be at least 1")
    private Integer totalSteps;

    @NotEmpty(message = "Coins list cannot be empty")
    private List<String> coins;

//    @NotBlank(message = "Side cannot be empty")
//    @Pattern(regexp = "^(BUY|SELL)$", message = "Side must be either BUY or SELL")
    private String side;

    private Boolean force;

    @NotNull(message = "Discount percentage must be specified")
    @DecimalMin(value = "0.0", message = "Discount percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount percentage cannot exceed 100")
    private Double discountPct;

    @NotNull(message = "Randomness percentage must be specified")
    @DecimalMin(value = "0.0", message = "Randomness percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Randomness percentage cannot exceed 100")
    private Double randomnessPct;
} 