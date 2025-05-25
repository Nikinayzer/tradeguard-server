package korn03.tradeguardserver.endpoints.dto.user.exchangeAccount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeAccountDTO {
    private Long id;
    private String provider;
    private String name;
    private String readWriteApiKey;
    private String readWriteApiSecret;
}
