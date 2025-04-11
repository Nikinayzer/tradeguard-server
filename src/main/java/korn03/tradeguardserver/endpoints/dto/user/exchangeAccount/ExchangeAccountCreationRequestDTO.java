package korn03.tradeguardserver.endpoints.dto.user.exchangeAccount;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeAccountCreationRequestDTO {
    private String provider;
    private String name;
    private Boolean demo;
    private String readOnlyApiKey;
    private String readOnlyApiSecret;
    private String readWriteApiKey;
    private String readWriteApiSecret;
}
