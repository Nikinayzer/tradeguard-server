package korn03.tradeguardserver.endpoints.dto.user;

import lombok.Data;

@Data
public class BybitAccountRequestDTO {
    private String name;
    private String readOnlyApiKey;
    private String readOnlyApiSecret;
    private String readWriteApiKey;
    private String readWriteApiSecret;
} 