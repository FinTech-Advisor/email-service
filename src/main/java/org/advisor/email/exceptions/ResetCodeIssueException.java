package org.advisor.email.exceptions;

import org.advisor.global.exceptions.BadRequestException;

public class ResetCodeIssueException extends BadRequestException {
    public ResetCodeIssueException() {
        super("Fail.resetCode.issue");
        setErrorCode(true);
    }
}