package korn03.tradeguardserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Missing or invalid JWT token");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}