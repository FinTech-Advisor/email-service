package org.advisor.email.exceptions;

import org.advisor.global.exceptions.BadRequestException;

public class ResetCodeExpiredException extends BadRequestException {
    public ResetCodeExpiredException() {
        super("Expired.resetCode");
        setErrorCode(true);
    }
}