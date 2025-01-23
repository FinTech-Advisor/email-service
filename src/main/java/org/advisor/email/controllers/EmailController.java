package org.advisor.email.controllers;

import lombok.RequiredArgsConstructor;
import org.advisor.email.exceptions.AuthCodeIssueException;
import org.advisor.email.services.EmailAuthService;
import org.advisor.email.services.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Profile("email")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vi/email")
public class EmailController {
    private final EmailAuthService authService;
    private final EmailService emailService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/auth/{to}")
    public void authCode(@PathVariable("to") String to) {
        if (!authService.sendCode(to)) {
            throw new AuthCodeIssueException();
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @GetMapping("/verify")
    public void verify(@RequestParam(name="authCode", required = false) Integer authCode) {
        authService.verify(authCode);
    }


    @PostMapping({"", "/tpl/{tpl}"})
    public void sendEmail(@PathVariable(name="tpl", required = false) String tpl, @RequestPart(name="file", required = false) List<MultipartFile> files, @ModelAttribute RequestEmail form) {
        form.setFiles(files);
        tpl = StringUtils.hasText(tpl) ? tpl : "general";
        emailService.sendEmail(form, tpl);
    }


}