package com.wikex.wikex.sms.support;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.wikex.wikex.sms.EmailProvider;
import com.wikex.wikex.util.MessageResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class AliStmpEmailProvider implements EmailProvider {


    private String alistmp_host;
    private String alistmp_port;
    private String alistmp_from;
    private String alistmp_password;

    public AliStmpEmailProvider(String alistmp_host,String alistmp_port,String alistmp_from,String alistmp_password) {
        this.alistmp_host = alistmp_host;
        this.alistmp_port = alistmp_port;
        this.alistmp_from = alistmp_from;
        this.alistmp_password = alistmp_password;
    }

    @Override
    public MessageResult sendEmail(String email, String code, String subject, String templateName) throws Exception {
        
        String smtpHost = "smtpdm-ap-southeast-1.aliyun.com";  
        String smtpPort = "465";  
        String fromEmail = "no-reply@kaiyuan.xin";  
        String smtpPassword = "dAIt64tTAlCoZAEsIP";  

        
        Properties properties = new Properties();
        properties.put("mail.smtp.host", alistmp_host);  
        properties.put("mail.smtp.port", alistmp_port);  
        properties.put("mail.smtp.auth", "true");    
        properties.put("mail.smtp.ssl.enable", "true");  

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(alistmp_from, alistmp_password);
            }
        });

        try {
            Map<String, Object> model = new HashMap<>(16);
            model.put("code", code);
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
            cfg.setClassForTemplateLoading(this.getClass(), "/templates");
            Template template = cfg.getTemplate(templateName);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(alistmp_from));  
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));  
            message.setSubject(subject);  
            
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(html, "text/html; charset=UTF-8");  

            
            MimeMultipart mimeMultipart = new MimeMultipart();
            mimeMultipart.addBodyPart(mimeBodyPart);

            
            message.setContent(mimeMultipart);

            
            Transport.send(message);
            

        } catch (MessagingException e) {
            return MessageResult.error("email sending failed!");
        }
        return MessageResult.success("SUCCESS");
    }
}
