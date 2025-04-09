package korn03.tradeguardserver.endpoints.dto.auth;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDTO {
    private String token;
    private UserDTO user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserDTO {
        private String username;
        private String email;
        private String firstName;
    }
}
