package com.wikex.wikex.sms.support;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
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
public class AliyunEmailProvider implements EmailProvider {

    private IClientProfile profile = null;
    private IAcsClient client = null;

    @Value("${aliyun.mail-sms.from-address}")
    private String emailFromAddress;
    @Value("${aliyun.mail-sms.from-alias}")
    private String emailAlias;
    @Value("${aliyun.mail-sms.email-tag}")
    private String emailTag;

    private String e_Region;
    private String e_accessKeyId;
    private String e_accessSecret;

    private SingleSendMailRequest request = new SingleSendMailRequest();

    public AliyunEmailProvider() {
    }

    public AliyunEmailProvider(String e_Region, String e_accessKeyId, String e_accessSecret) {

        profile = DefaultProfile.getProfile(e_Region, e_accessKeyId, e_accessSecret);
        if (!"cn-hangzhou".equals(e_Region)) {
            try {
                DefaultProfile.addEndpoint("dm." + e_Region + ".aliyuncs.com", e_Region, "Dm",
                        "dm." + e_Region + ".aliyuncs.com");
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
        client = new DefaultAcsClient(profile);
    }

    public static void main(String[] args) {

    }

    @Override
    @Async
    public MessageResult sendEmail(String email, String code, String subject, String templateName) throws Exception {
        Map<String, Object> model = new HashMap<>(16);
        model.put("code", code);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate(templateName);
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        if (!"cn-hangzhou".equals(e_Region)) {
            request.setVersion("2017-06-22");
        }
        request.setAccountName(emailFromAddress);
        request.setFromAlias(emailAlias);
        request.setAddressType(1);
        request.setTagName(emailTag);
        request.setReplyToAddress(true);
        request.setToAddress(email);

        request.setSubject(subject);

        request.setHtmlBody(html);

        request.setMethod(MethodType.POST);

        SingleSendMailResponse httpResponse = client.getAcsResponse(request);

        return MessageResult.success();
    }
}
