package org.advisor.email.exceptions;

import org.advisor.global.exceptions.BadRequestException;

public class AuthCodeIssueException extends BadRequestException {
    public AuthCodeIssueException() {
        super("Fail.authCode.issue");
        setErrorCode(true);
    }
}