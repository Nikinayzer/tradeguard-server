package korn03.tradeguardserver.endpoints.dto.user.settings.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecuritySettingsDTO {
    private boolean twoFactorEnabled;
}
