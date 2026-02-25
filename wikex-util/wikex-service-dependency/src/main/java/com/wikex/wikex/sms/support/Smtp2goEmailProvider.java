package com.wikex.wikex.sms.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.wikex.wikex.sms.EmailProvider;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.MessageResult;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Smtp2goEmailProvider implements EmailProvider {

    @Value("${smtp2go.username}")
    private String smtpUsername;
    @Value("${smtp2go.password}")
    private String smtpPassword;
    @Value("${smtp2go.from}")
    private String emailFromAddress;
    @Value("${smtp2go.alias}")
    private String fromAlias;

    public Smtp2goEmailProvider() {
    }

    @Override
    @Async
    public MessageResult sendEmail(String email, String code, String subject, String templateName) throws Exception {
        // Cấu hình Properties cho SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "mail.smtp2go.com");
        props.put("mail.smtp.port", "2525");
        props.put("mail.mime.charset", "UTF-8"); // Fix toàn bộ encoding
        props.put("mail.mime.encodefilename", "true"); // Cho attachment nếu có sau này
        // Tạo Authenticator
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        };

        // Tạo Session
        Session session = Session.getInstance(props, auth);

        // Tạo Message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailFromAddress, fromAlias != null ? fromAlias : "Wikex", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
        message.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));

        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        model.put("email", email.split("@")[0]);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        cfg.setDefaultEncoding("UTF-8");
        Template template = cfg.getTemplate(templateName);
        String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        // Nếu có HTML, tạo multipart (text + HTML)
        String textBody = "Your code is: " + code; // Nội dung text đơn giản
        if (htmlBody != null) {
            Multipart multipart = new MimeMultipart("alternative");
            BodyPart textPart = new MimeBodyPart();
            textPart.setContent(textBody, "text/plain; charset=utf-8");
            multipart.addBodyPart(textPart);

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
        } else {
            // Chỉ text
            message.setText(textBody);
        }

        // Gửi email
        Transport.send(message);
        log.info("Email sent successfully to " + email);
        return MessageResult.success();
    }

}
