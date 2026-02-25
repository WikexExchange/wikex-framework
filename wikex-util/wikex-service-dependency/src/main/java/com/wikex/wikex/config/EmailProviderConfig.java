package com.wikex.wikex.config;

import com.wikex.wikex.sms.EmailProvider;
import com.wikex.wikex.sms.support.AliStmpEmailProvider;
import com.wikex.wikex.sms.support.AliyunEmailProvider;
import com.wikex.wikex.sms.support.JaveEmailProvider;
import com.wikex.wikex.sms.support.Smtp2goEmailProvider;
import com.wikex.wikex.sms.support.TestEmailProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailProviderConfig {

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

    @Value("${alistmp.host}")
    private String alistmp_host;
    @Value("${alistmp.port}")
    private String alistmp_port;
    @Value("${alistmp.from}")
    private String alistmp_from;
    @Value("${alistmp.password}")
    private String alistmp_password;

    @Bean
    public EmailProvider getEmailProvider(@Value("${email.driver:}") String driverName) {
        EmailProvider provider = null;
        switch (driverName) {
            case "java":
                provider = new JaveEmailProvider(from, company);
                break;
            case "aliyun":
                provider = new AliyunEmailProvider(e_Region, e_accessKeyId, e_accessSecret);
                break;
            case "alistmp":
                provider = new AliStmpEmailProvider(alistmp_host, alistmp_port, alistmp_from, alistmp_password);
                break;
            case "test":
                provider = new TestEmailProvider();
                break;
            case "smtp2go":
                provider = new Smtp2goEmailProvider();
                break;
        }
        return provider;
    }
}
