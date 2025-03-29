package korn03.tradeguardserver.exception.job;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class JobStateException extends RuntimeException {
    public JobStateException(String message) {
        super(message);
    }
}
