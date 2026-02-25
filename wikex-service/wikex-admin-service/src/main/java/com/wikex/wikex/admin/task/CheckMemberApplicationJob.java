package com.wikex.wikex.admin.task;

import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.user.feign.MemberApplicationFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.util.MessageResult;
import com.xxl.job.core.handler.annotation.XxlJob;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

 // Check real-name authentication application users
@Component
@Slf4j
public class CheckMemberApplicationJob {
	@Autowired
	private MemberFeign memberService;

	@Autowired
    private MemberApplicationFeign memberApplicationService;

	@Autowired
    private SMSProvider smsProvider;

	@Resource
    private JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
    private String from;
    @Value("${spark.system.host}")
    private String host;
    @Value("${spark.system.name}")
    private String company;

	@Value("${spark.system.admins}")
    private String admins;

    @Value("${spark.system.admin_phones}")
    private String adminPhones;

    private Long maxUserId = Long.valueOf(0);

	/**
	 * Check once every hour
	 */
	//@Scheduled(cron = "0 0 * * * *")
	@XxlJob("checkNewMemberApplication")
    public void checkNewMemberApplication(){
		if(isRestTime()) {
			return;
		}
		// Query the number of pending reviews
		Integer count = memberApplicationService.countAuditing();
		if(count > 0) {
			try {
				String[] adminList = admins.split(",");
				for(int i = 0; i < adminList.length; i++) {
					sendEmailMsg(adminList[i], "There are new real-name authentication applications ( Total " + count+ " )", "New Real-Name Authentication Review Notification");
				}
			} catch (Exception e) {
				MessageResult result;
				try {
					String[] phones = adminPhones.split(",");
					if(phones.length > 0) {
						result = smsProvider.sendSingleMessage(phones[0], "==New Real-Name Application==");
						if(result.getCode() != 0) {
							if(phones.length > 1) {
								smsProvider.sendSingleMessage(phones[1], "==New Real-Name Application==");
							}
						}
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

	@Async
    public void sendEmailMsg(String email, String msg, String subject) throws MessagingException, IOException, TemplateException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        helper.setTo(email);
        helper.setSubject(company + "-" + subject);
        Map<String, Object> model = new HashMap<>(16);
        model.put("msg", msg);
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        Template template = cfg.getTemplate("simpleMessage.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        helper.setText(html, true);

        //Send email
        javaMailSender.send(mimeMessage);
        
    }


	private boolean isRestTime() {
		Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Different trading volumes are required at different times of the day

        if(hour >= 0 && hour <= 6) {
        	return true;
        }
        return false;
	}
}
