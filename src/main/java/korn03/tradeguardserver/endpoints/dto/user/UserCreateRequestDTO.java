package korn03.tradeguardserver.endpoints.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDTO {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
} 