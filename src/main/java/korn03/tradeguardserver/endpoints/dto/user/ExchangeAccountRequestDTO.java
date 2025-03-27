package korn03.tradeguardserver.endpoints.dto.user;

import lombok.Data;

@Data
public class ExchangeAccountRequestDTO {
    private Long id;
    private String name;
    private String provider;
    private String readOnlyApiKey;
    private String readOnlyApiSecret;
    private String readWriteApiKey;
    private String readWriteApiSecret;
} 