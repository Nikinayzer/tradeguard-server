package korn03.tradeguardserver.endpoints.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String oldPassword;
    private String newPassword;
} 