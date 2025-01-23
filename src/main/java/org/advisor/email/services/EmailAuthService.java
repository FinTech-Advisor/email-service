package org.advisor.email.services;

import lombok.RequiredArgsConstructor;
import org.advisor.email.controllers.RequestEmail;
import org.advisor.email.exceptions.AuthCodeExpiredException;
import org.advisor.email.exceptions.AuthCodeMismatchException;
import org.advisor.global.exceptions.BadRequestException;
import org.advisor.global.libs.Utils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Profile("emaill")
@RequiredArgsConstructor
public class EmailAuthService {
    private final Utils utils;
    private final EmailService emailService;

    public boolean sendCode(String to) {
        Random random = new Random();
        String subject = utils.getMessage("Email.authCode.subject");


        Integer authCode = random.nextInt(10000, 99999);

        LocalDateTime expired = LocalDateTime.now().plusMinutes(3L);

        utils.saveValue(utils.getUserHash() + "_authCode", authCode);
        utils.saveValue(utils.getUserHash() + "_expiredTime", expired);

        Map<String, Object> tplData = new HashMap<>();
        tplData.put("authCode", authCode);

        RequestEmail form = new RequestEmail();
        form.setTo(List.of(to));
        form.setSubject(subject);

        return emailService.sendEmail(form, "auth", tplData);
    }

    public void verify(Integer code) {
        if (code == null) {
            throw new BadRequestException(utils.getMessage("NotBlank.authCode"));
        }

        LocalDateTime expired = utils.getValue(utils.getUserHash() + "_expiredTime");
        Integer authCode = utils.getValue(utils.getUserHash() + "_authCode");

        if (expired != null && expired.isBefore(LocalDateTime.now())) {
            throw new AuthCodeExpiredException();
        }

        if (authCode == null) {
            throw new BadRequestException();
        }

        if (!code.equals(authCode)) {
            throw new AuthCodeMismatchException();
        }

        utils.saveValue(utils.getUserHash() + "_authCodeVerified", true);
    }
}