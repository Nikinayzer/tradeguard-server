package korn03.tradeguardserver.endpoints.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {
    @NotNull //todo continue for all dtos
    private String username;
    @NotNull
    private String password;
}
