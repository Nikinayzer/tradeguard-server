package korn03.tradeguardserver.exception.registration;

import korn03.tradeguardserver.exception.ApiException;

public class UsernameTakenException extends ApiException {
    public UsernameTakenException(String message) { super("username_taken", message); }
}