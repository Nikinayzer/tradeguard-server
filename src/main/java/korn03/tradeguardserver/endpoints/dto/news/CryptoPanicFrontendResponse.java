package korn03.tradeguardserver.endpoints.dto.news;

import lombok.Data;
import java.util.List;

@Data
public class CryptoPanicFrontendResponse {
    private int count;
    private List<CryptoPanicPost> results;

    public static CryptoPanicFrontendResponse from(CryptoPanicResponse response) {
        CryptoPanicFrontendResponse cryptoPanicFrontendResponse = new CryptoPanicFrontendResponse();
        cryptoPanicFrontendResponse.setCount(response.getCount());
        cryptoPanicFrontendResponse.setResults(response.getResults());
        return cryptoPanicFrontendResponse;
    }
} 