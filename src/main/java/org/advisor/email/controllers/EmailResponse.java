package org.advisor.email.controllers;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailResponse {
    private String message;

    public EmailResponse(String message) {
        this.message = message;
    }
}