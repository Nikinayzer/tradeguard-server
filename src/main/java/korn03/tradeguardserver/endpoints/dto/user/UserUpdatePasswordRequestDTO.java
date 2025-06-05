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
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date of birth must be in format yyyy-MM-dd")
    private String dateOfBirth; // Only for unauthenticated users
}
