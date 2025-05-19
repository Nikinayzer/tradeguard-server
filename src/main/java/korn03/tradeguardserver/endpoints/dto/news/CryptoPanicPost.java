package korn03.tradeguardserver.endpoints.dto.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoPanicPost {
    private String kind;
    private String domain;
    private Source source;
    private String title;
    private String published_at;
    private String slug;
    private List<Currency> currencies;
    private long id;
    private String url;
    private String created_at;
    private Votes votes;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String title;
        private String region;
        private String domain;
        private String path;
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Currency {
        private String code;
        private String title;
        private String slug;
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Votes {
        private int negative;
        private int positive;
        private int important;
        private int liked;
        private int disliked;
        private int lol;
        private int toxic;
        private int saved;
        private int comments;
    }
} 