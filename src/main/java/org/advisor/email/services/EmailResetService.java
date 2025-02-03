package org.advisor.email.services;

import lombok.RequiredArgsConstructor;
import org.advisor.email.controllers.RequestEmail;
import org.advisor.email.exceptions.InvalidTokenException;
import org.advisor.global.libs.Utils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailResetService {

    private final Utils utils;
    private final EmailService emailService;

    public void sendResetEmail(String to) {
        String token = UUID.randomUUID().toString();

        LocalDateTime expired = LocalDateTime.now().plusHours(1); // 1시간 후 만료
        utils.saveValue(utils.getUserHash() + "_resetToken", token);
        utils.saveValue(utils.getUserHash() + "_resetTokenExpired", expired);

        String fullResetUrl = generateResetUrl(token); // URL 생성 (필요 시 수정)

        Map<String, Object> variables = new HashMap<>();
        variables.put("resetUrl", fullResetUrl);

        RequestEmail requestEmail = new RequestEmail(
                Collections.singletonList(to),
                null,
                null,
                utils.getMessage("Email.reset.subject"), // 이메일 제목
                null,
                variables,
                null
        );

        emailService.sendEmail(requestEmail, "reset-email"); // 템플릿 이름 (필요 시 수정)
    }

    public void confirmResetEmail(String token) {
        LocalDateTime expired = utils.getValue(utils.getUserHash() + "_resetTokenExpired");
        String storedToken = utils.getValue(utils.getUserHash() + "_resetToken");

        if (expired == null || expired.isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("만료된 토큰입니다.");
        }

        if (storedToken == null || !storedToken.equals(token)) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }

        // TODO: 이메일/비밀번호 초기화 로직

        // 토큰 무효화
        utils.removeValue(utils.getUserHash() + "_resetToken");
        utils.removeValue(utils.getUserHash() + "_resetTokenExpired");
    }

    // URL 생성 로직 (필요에 따라 수정)
    private String generateResetUrl(String token) {
        return "http://localhost:8080/reset-password/verify?token=" + token;
    }
}