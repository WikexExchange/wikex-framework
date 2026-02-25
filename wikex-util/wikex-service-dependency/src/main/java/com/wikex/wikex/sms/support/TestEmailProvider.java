package com.wikex.wikex.sms.support;

import com.wikex.wikex.sms.EmailProvider;
import com.wikex.wikex.util.MessageResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class TestEmailProvider implements EmailProvider {

    @Value("${spring.mail.username}")
    private String from;
    @Value("${spark.system.name}")
    private String company;

    @Value("${aliyun.mail-sms.region}")
    private String e_Region;
    @Value("${aliyun.mail-sms.access-key-id}")
    private String e_accessKeyId;
    @Value("${aliyun.mail-sms.access-secret}")
    private String e_accessSecret;


    @Override
    @Async
    public MessageResult sendEmail(String email, String code, String subject, String templateName) throws Exception {
        
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate(templateName);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        
        
        return MessageResult.success("SUCCESS");
    }
}
