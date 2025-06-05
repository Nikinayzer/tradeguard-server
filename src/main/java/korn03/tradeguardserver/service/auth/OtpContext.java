package korn03.tradeguardserver.service.auth;

import lombok.Getter;

@Getter
public enum OtpContext {
    SIGN_UP("sign up"),
    LOGIN("authorization"),
    PASSWORD_RESET("password change");

    // Variable to set reason in html template
    private final String reason;

    OtpContext(String reason) {
        this.reason = reason;
    }
}
