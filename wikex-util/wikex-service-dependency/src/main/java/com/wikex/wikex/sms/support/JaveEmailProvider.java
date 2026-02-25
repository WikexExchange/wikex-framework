package com.wikex.wikex.sms.support;


import com.wikex.wikex.sms.EmailProvider;
import com.wikex.wikex.util.MessageResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class JaveEmailProvider implements EmailProvider {

    @Autowired
    private JavaMailSender javaMailSender;

    private String from;
    private String company;

    public JaveEmailProvider() {
    }

    public JaveEmailProvider(String from, String company) {
        this.from = from;
        this.company = company;
    }

    @Override
    @Async
    public MessageResult sendEmail(String email, String code, String subject, String templateName) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company);
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate(templateName);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        helper.setText(html, true);
        javaMailSender.send(mimeMessage);
        return MessageResult.success();
    }
}
