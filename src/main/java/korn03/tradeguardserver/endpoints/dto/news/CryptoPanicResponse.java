package korn03.tradeguardserver.endpoints.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoPanicResponse {
    private int count;
    private String next;
    private String previous;
    private List<CryptoPanicPost> results;
} 