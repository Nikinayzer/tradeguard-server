package korn03.tradeguardserver.service.auth;

import lombok.Getter;

@Getter
public enum OtpContext {
    SIGN_UP,
    LOGIN,
    ACCOUNT_DELETION
}
