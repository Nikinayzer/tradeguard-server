package korn03.tradeguardserver.endpoints.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {
    @NotNull
    private String identifier; // username or email
    @NotNull
    private String password;
}
