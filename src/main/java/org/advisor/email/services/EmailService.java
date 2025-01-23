package org.advisor.email.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.advisor.email.controllers.RequestEmail;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
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
@Profile("email")
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

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

            javaMailSender.send(message);

            return true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return false;
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