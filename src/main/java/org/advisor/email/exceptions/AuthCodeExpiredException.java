package org.advisor.email.exceptions;

import org.advisor.global.exceptions.BadRequestException;

public class AuthCodeExpiredException extends BadRequestException {
    public AuthCodeExpiredException() {
        super("Expired.authCode");
        setErrorCode(true);
    }
}