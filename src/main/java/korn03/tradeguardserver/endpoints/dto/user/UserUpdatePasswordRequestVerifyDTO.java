package korn03.tradeguardserver.endpoints.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePasswordRequestVerifyDTO {
    @NotNull
    private String code;
    @NotNull
    private String email;
    @NotNull
    @Size(min = 8, message = "Password must be at least 8 characters long") //todo check in service
    private String newPassword;
}
