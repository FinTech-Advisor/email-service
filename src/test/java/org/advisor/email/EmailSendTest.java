package org.advisor.email;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.advisor.email.controllers.RequestEmail;
import org.advisor.email.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles({"default", "test", "email"})
public class EmailSendTest {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private EmailService service;

    @Test
    void test1() throws Exception {
        /**
         * to : 받는 이메일
         * cc : 참조
         * bcc : 숨은 참조
         */
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo("kwat0112@gmail.com");
        helper.setSubject("테스트 이메일 제목...");
        helper.setText("테스트 이메일 내용...");
        javaMailSender.send(message);
    }

    @Test
    void test2() {
        Context context = new Context();
        context.setVariable("subject", "테스트 제목...");

        String text = templateEngine.process("email/auth", context);

        System.out.println(text);
    }

    @Test
    void test3() {
        RequestEmail form = new RequestEmail();
        form.setTo(List.of("kwat0112@gmail.com", "kwat0112@gmail.com"));
        form.setCc(List.of("kwat0112@gmail.com"));
        form.setBcc(List.of("kwat0112@gmail.com"));
        form.setSubject("테스트 이메일 제목...");
        form.setContent("<h1>테스트 이메일 내용...</h1>");

        Map<String, Object> tplData = new HashMap<>();
        tplData.put("key1", "값1");
        tplData.put("key2", "값2");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append((int) (Math.random() * 10)); // 0~9 사이의 랜덤 정수 생성
        }
        String authCode = sb.toString();
        tplData.put("authCode", authCode);

        boolean result = service.sendEmail(form, "auth", tplData);
        System.out.println(result);
    }
}