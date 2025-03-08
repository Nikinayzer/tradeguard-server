package korn03.tradeguardserver.endpoints.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BybitAccountDTO {
    private Long id;
    private Long userId;
    private String name;
    // All below masked like *******iWtkMst9
    private String readOnlyApiKey;
    private String readOnlyApiSecret;
    private String readWriteApiKey;
    private String readWriteApiSecret;
} 