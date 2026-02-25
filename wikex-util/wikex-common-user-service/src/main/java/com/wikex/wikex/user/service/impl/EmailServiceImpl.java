package com.wikex.wikex.user.service.impl;

import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.sms.EmailProvider;
import com.wikex.wikex.user.service.EmailService;
import com.wikex.wikex.util.GeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static org.springframework.util.Assert.notNull;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private EmailProvider emailProvider;

    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    @Autowired
    private RedisTemplate redisTemplate;

    private String getTemplateByLang(String lang, String type) {
        String normalizedLang = (lang == null ? "" : lang.toLowerCase());

        switch (normalizedLang) {
            case "en_us":
                switch (type) {
                    case "bind":
                        return "bindCodeEmailEn.ftl";
                    case "reset":
                        return "resetPasswordCodeEmailEn.ftl";
                    case "change":
                        return "changePasswordCodeEmailEn.ftl";
                    case "welcome":
                        return "welcomeEmailEn.ftl";
                    case "setup":
                        return "setupPasswordCodeEmailEn.ftl";
                    case "bind_google":
                        return "bindGoogleEn.ftl";
                    case "bind_apple":
                        return "bindAppleEn.ftl";
                    case "unbind_google":
                        return "unbindGoogleEn.ftl";
                    case "unbind_apple":
                        return "unbindAppleEn.ftl";
                    case "enable_2fa":
                        return "enable2FAEn.ftl";
                    case "disable_2fa":
                        return "disable2FAEn.ftl";
                    case "deposit_created":
                        return "depositCreatedEn.ftl";
                    case "deposit_success":
                        return "depositSuccessEn.ftl";
                    default:
                        return "bindCodeEmailEn.ftl";
                }

            case "vi_vn":
            default:
                switch (type) {
                    case "bind":
                        return "bindCodeEmailVi.ftl";
                    case "reset":
                        return "resetPasswordCodeEmailVi.ftl";
                    case "change":
                        return "changePasswordCodeEmailVi.ftl";
                    case "welcome":
                        return "welcomeEmailVi.ftl";
                    case "setup":
                        return "setupPasswordCodeEmailVi.ftl";
                    case "bind_google":
                        return "bindGoogleVi.ftl";
                    case "bind_apple":
                        return "bindAppleVi.ftl";
                    case "unbind_google":
                        return "unbindGoogleVi.ftl";
                    case "unbind_apple":
                        return "unbindAppleVi.ftl";
                    case "enable_2fa":
                        return "enable2FAVi.ftl";
                    case "disable_2fa":
                        return "disable2FAVi.ftl";
                    case "deposit_created":
                        return "depositCreatedVi.ftl";
                    case "deposit_success":
                        return "depositSuccessVi.ftl";
                    default:
                        return "bindCodeEmailVi.ftl";
                }
        }
    }

    private String getSubjectByLang(String lang) {
        if (lang == null)
            lang = "vi_vn";
        String normalizedLang = lang.toLowerCase();

        switch (normalizedLang) {
            case "en_us":
                return "Wikex Verification Code (OTP)";
            case "vi_vn":
                return "Wikex gửi mã xác thực OTP";
            default:
                return "Wikex Verification Code (OTP)";
        }
    }

    private void sentEmailCode(String prefix, String email, String lang, String templateName) throws Exception {
        String code = String.valueOf(GeneratorUtil.getRandomNumber(100000, 999999));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // if (valueOperations.get(prefix + email) != null) {
        // return;
        // }
        String subject = getSubjectByLang(lang);

        emailProvider.sendEmail(email, code, subject, templateName);
        valueOperations.set(prefix + email, code, 10, TimeUnit.MINUTES);
    }

    @Async
    @Override
    public void sentEmailAddCode(String email, String lang) throws Exception {
        this.sentEmailCode(SysConstant.ADD_ADDRESS_CODE_PREFIX, email, lang, "addAddressCodeEmail.ftl");
    }

    @Async
    @Override
    public void sentEmailWelcome(String email, String lang) throws Exception {
        String subject = "";
        String templateName = getTemplateByLang(lang, "welcome");
        switch (lang) {
            case "en_US":
                subject = "Welcome to Wikex!";
                break;
            case "vi_VN":
                subject = "Chào mừng bạn đến với Wikex!";
                break;
            default:
                subject = "Welcome to Wikex!";
                break;
        }

        emailProvider.sendEmail(email, "", subject, templateName);
    }

    @Async
    @Override
    public void sentEmailDepositCreated(String email, String lang) throws Exception {
        String subject = "";
        String templateName = getTemplateByLang(lang, "welcome");
        switch (lang) {
            case "en_US":
                subject = "Deposit Initiated on Wikex!";
                break;
            case "vi_VN":
                subject = "Yêu cầu nạp tài sản mã hóa đã được tạo trên Wikex!";
                break;
            default:
                subject = "Deposit Initiated on Wikex!";
                break;
        }

        emailProvider.sendEmail(email, "", subject, templateName);
    }

    @Async
    @Override
    public void sentEmailDepositSuccess(String email, String lang) throws Exception {
        String subject = "";
        String templateName = getTemplateByLang(lang, "welcome");
        switch (lang) {
            case "en_US":
                subject = "Deposit Successful on Wikex!";
                break;
            case "vi_VN":
                subject = "Nạp tài sản mã hóa thành công trên Wikex!";
                break;
            default:
                subject = "Deposit Successful on Wikex!";
                break;
        }

        emailProvider.sendEmail(email, "", subject, templateName);
    }

    @Async
    @Override
    public void sentBindEmailCode(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "bind");
        this.sentEmailCode(SysConstant.EMAIL_BIND_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sentChangePassword(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "change");
        this.sentEmailCode(SysConstant.CHANGE_PASSWORD_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sentResetPassword(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "reset");
        this.sentEmailCode(SysConstant.RESET_PASSWORD_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sentSetupPassword(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "setup");
        this.sentEmailCode(SysConstant.SETUP_PASSWORD_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sentUntieEmailCode(String email, String lang) throws Exception {
        this.sentEmailCode(SysConstant.EMAIL_UNTIE_CODE_PREFIX, email, lang, "resetPasswordCodeEmail.ftl");
    }

    @Async
    @Override
    public void sentUpdateEmailCode(String email, String lang) throws Exception {
        this.sentEmailCode(SysConstant.EMAIL_UPDATE_CODE_PREFIX, email, lang, "resetPasswordCodeEmail.ftl");
    }

    @Async
    @Override
    public void sentRegEmailCode(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "bind");
        this.sentEmailCode(SysConstant.EMAIL_REG_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sendEnable2FA(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "enable_2fa");
        this.sentEmailCode(SysConstant.ENABLE_2FA_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sendDisable2FA(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "disable_2fa");
        this.sentEmailCode(SysConstant.DISABLE_2FA_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sendBindGoogleEmail(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "bind_google");
        this.sentEmailCode(SysConstant.BIND_GOOGLE_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sendBindAppleEmail(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "bind_apple");
        this.sentEmailCode(SysConstant.BIND_APPLE_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sendUnbindGoogleEmail(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "unbind_google");
        this.sentEmailCode(SysConstant.UNBIND_GOOGLE_CODE_PREFIX, email, lang, templateName);
    }

    @Async
    @Override
    public void sendUnbindAppleEmail(String email, String lang) throws Exception {
        String templateName = getTemplateByLang(lang, "unbind_apple");
        this.sentEmailCode(SysConstant.UNBIND_APPLE_CODE_PREFIX, email, lang, templateName);
    }

    @Override
    public boolean checkCode4ChangePassword(String account, String code) {
        notNull(code, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.CHANGE_PASSWORD_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    @Override
    public boolean checkCode4forgetPassword(String account, String code) {
        notNull(code, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.RESET_PASSWORD_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    @Override
    public boolean checkCode4SetupPassword(String account, String code) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.SETUP_PASSWORD_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    public boolean checkCode4LinkGoogle(String account, String code) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.BIND_GOOGLE_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    @Override
    public boolean checkCode4LinkApple(String account, String code) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.BIND_APPLE_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    public boolean checkCode4UnlinkGoogle(String account, String code) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.UNBIND_GOOGLE_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    @Override
    public boolean checkCode4UnlinkApple(String account, String code) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.UNBIND_APPLE_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    @Override
    public boolean checkCode4Disable2FA(String account, String code) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(SysConstant.DISABLE_2FA_CODE_PREFIX + account);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_EXPIRED"));
        return code.equals(redisCode.toString());
    }

    @Override
    public boolean checkCode4Reg(String email, String code) {
        return checkCode(SysConstant.EMAIL_REG_CODE_PREFIX + email, code);
    }

    private boolean checkCode(String key, String code) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(key);
        notNull(redisCode, localeMessageSourceService.getMessage("VERIFICATION_CODE_NOT_EXISTS"));
        if (!code.equals(redisCode.toString())) {
            return false;
        } else {
            valueOperations.getOperations().delete(key);
            return true;
        }
    }

    public boolean checkVerificationCode4Email(String email, String code, String type) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCode = valueOperations.get(type + email);
        if (redisCode == null || !code.equals(redisCode.toString())) {
            return false;
        } else {
            return true;
        }
    }
}
