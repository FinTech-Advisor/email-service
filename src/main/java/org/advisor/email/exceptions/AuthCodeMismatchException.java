package org.advisor.email.exceptions;

import org.advisor.global.exceptions.BadRequestException;

public class AuthCodeMismatchException extends BadRequestException {
    public AuthCodeMismatchException() {
        super("Mismatch.authCode");
        setErrorCode(true);
    }
}