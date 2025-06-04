package korn03.tradeguardserver.endpoints.dto.auth.twofactor;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequestDTO {
    @NotNull
    private String email;
    @NotNull
    private String code;
}