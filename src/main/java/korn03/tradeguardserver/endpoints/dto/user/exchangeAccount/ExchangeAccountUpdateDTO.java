package korn03.tradeguardserver.endpoints.dto.user.exchangeAccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeAccountUpdateDTO {
    private String name;
    private String readOnlyApiKey;
    private String readOnlyApiSecret;
    private String readWriteApiKey;
    private String readWriteApiSecret;
}
