package org.advisor.email.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.advisor.email.controllers.RequestEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Lazy
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public boolean sendEmail(RequestEmail form, String tpl, Map<String, Object> tplData) {

        try {
            Context context = new Context();
            tplData = Objects.requireNonNullElseGet(tplData, HashMap::new);

            List<String> to = form.getTo();
            List<String> cc = form.getCc();
            List<String> bcc = form.getBcc();
            String subject = form.getSubject();
            String content = form.getContent();
            List<MultipartFile> files = form.getFiles();

            tplData.put("to", to);
            tplData.put("cc", cc);
            tplData.put("bcc", bcc);
            tplData.put("subject", subject);
            tplData.put("content", content);

            context.setVariables(tplData);

            String html = templateEngine.process("email/" + tpl, context);

            boolean isFileAttached = files != null && !files.isEmpty();

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, isFileAttached, "UTF-8");
            helper.setTo(form.getTo().toArray(String[]::new));

            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(String[]::new));
            }

            if (bcc != null && !bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(String[]::new));
            }

            helper.setSubject(subject);
            helper.setText(html, true);

            // 파일 첨부 처리 S
            if (isFileAttached) {
                for (MultipartFile file : files) {
                    helper.addAttachment(file.getOriginalFilename(), file);
                }
            }
            // 파일 첨부 처리 E

            logger.info("Sending email: to={}, subject={}", to, subject); // 받는 사람, 제목 로그 출력
            logger.info("Email message: {}", message); // message 객체 로그 출력
            javaMailSender.send(message);

            return true;
        } catch(Exception e) {
            logger.error("이메일 발송 실패", e); // 예외 정보 로그 출력
            return false;
        }
    }
    public boolean sendEmail(RequestEmail form, String tpl) {
        return sendEmail(form, tpl, form.getData());
    }

    public boolean sendEmail(String to, String subject, String content) {
        RequestEmail form = new RequestEmail();
        form.setTo(List.of(to));
        form.setSubject(subject);
        form.setContent(content);

        return sendEmail(form, "general");
    }
}