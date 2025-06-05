package korn03.tradeguardserver.exception.registration;

import korn03.tradeguardserver.exception.ApiException;

public class EmailTakenException extends ApiException {
    public EmailTakenException(String message) { super("email_taken", message); }
}
