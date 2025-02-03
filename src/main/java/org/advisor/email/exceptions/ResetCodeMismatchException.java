package org.advisor.email.exceptions;

import org.advisor.global.exceptions.BadRequestException;

public class ResetCodeMismatchException extends BadRequestException {
    public ResetCodeMismatchException() {
        super("Mismatch.resetCode");
        setErrorCode(true);
    }
}