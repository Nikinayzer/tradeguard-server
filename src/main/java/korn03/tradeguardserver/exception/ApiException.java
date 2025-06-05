package korn03.tradeguardserver.exception;

import lombok.Getter;
import org.hibernate.type.descriptor.DateTimeUtils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;


@Getter
public class ApiException extends RuntimeException {
    private final String code;
    private final String timestamp;

    public ApiException(String code, String message) {
        super(message);
        this.code = code;
        this.timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }
}
