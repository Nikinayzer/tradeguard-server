package korn03.tradeguardserver.endpoints.dto.auth.twofactor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// For future use in google authenticator or similar apps
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSetupDTO {
    private String secret;
    private String qrCode;
    private List<String> backupCodes;
}