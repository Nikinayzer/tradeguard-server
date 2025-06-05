package korn03.tradeguardserver.endpoints.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePasswordRequestDTO {
    @NotNull
    @Email(message = "Invalid email format")
    private String email;
}
