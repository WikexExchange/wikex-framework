package com.wikex.wikex.admin.controller.member;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.screen.MemberWalletScreen;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.user.dto.MemberWalletDTO;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.CoinFeign;
import com.wikex.wikex.user.feign.MemberFeign;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.DateUtil;
import com.wikex.wikex.util.MessageResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("member/member-wallet")
@Slf4j
public class MemberWalletController extends BaseAdminController {

    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private MemberFeign memberFeign;
    @Autowired
    private CoinFeign coinService;
    @Autowired
    private MemberTransactionFeign memberTransactionService;

    @Autowired
    private LocaleMessageSourceService messageSource;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${spark.system.admins}")
    private String admins;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${spark.system.host}")
    private String host;
    @Value("${spark.system.name}")
    private String company;

    @RequiresPermissions("member:member-wallet:recharge")
    @PostMapping("recharge")
    @AccessLog(module = AdminModule.MEMBER, operation = "Recharge management")
    public MessageResult recharge(
    		@SessionAttribute(SysConstant.SESSION_ADMIN) Admin admin,
            @RequestParam("unit") String unit,
            @RequestParam("uid") Long uid,
            @RequestParam("amount") BigDecimal amount) {
        // Front-end sends the coin name
        Coin coin = coinService.findByUnit(unit);
        if (coin == null) {
            return error(messageSource.getMessage("CURRENCY_NOT_FOUND"));
        }
        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(coin.getUnit(), uid);
        Assert.notNull(memberWallet, "wallet null");
        memberWalletService.increaseBalance(memberWallet.getId(),amount);
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(amount);
        memberTransaction.setMemberId(memberWallet.getMemberId());
        memberTransaction.setSymbol(coin.getUnit());
        memberTransaction.setType(TransactionType.ADMIN_RECHARGE.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransactionService.save(memberTransaction);

        String[] adminList = admins.split(",");
        for(int i = 0; i < adminList.length; i++) {
			sendEmailMsg(adminList[i], "Admin manual recharge (User ID: " + uid + ", Coin: " + unit + ", Amount: " + amount + "); Operator：" +admin.getUsername() + "/" + admin.getMobilePhone(), "Manual Recharge Notification");
		}

        return success(messageSource.getMessage("SUCCESS"));
    }

    /**
     * Send email
     * @param email
     * @param msg
     * @param subject
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    @Async
    public void sendEmailMsg(String email, String msg, String subject){
        try {
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

            // Send email
            javaMailSender.send(mimeMessage);
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresPermissions("member:member-wallet:balance")
    @PostMapping("balance")
    @AccessLog(module = AdminModule.MEMBER, operation = "Balance management")
    public MessageResult getBalance(
            MemberWalletScreen screen) {
        Page<MemberWalletDTO> page = memberWalletService.getBalance(screen);
        return success(messageSource.getMessage("SUCCESS"), IPage2Page(page));
    }

    @RequiresPermissions("member:member-wallet:reset-address")
    @PostMapping("reset-address")
    @AccessLog(module = AdminModule.MEMBER, operation = "Reset wallet address")
    public MessageResult resetAddress(String unit, long uid) {
        Member member = memberFeign.findMemberById(uid);
        Assert.notNull(member, "member null");
        try {
            JSONObject json = new JSONObject();
            json.put("uid", member.getId());
            json.put("unit", unit);
            
            rocketMQTemplate.convertAndSend("reset-member-address", json.toJSONString());
            return MessageResult.success(messageSource.getMessage("SUCCESS"));
        } catch (Exception e) {
            return MessageResult.error(messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:member-wallet:lock-wallet")
    @PostMapping("lock-wallet")
    @AccessLog(module = AdminModule.MEMBER, operation = "Lock wallet")
    public MessageResult lockWallet(Long uid, String unit) {
        if (memberWalletService.lockWallet(uid, unit)) {
            return success(messageSource.getMessage("SUCCESS"));
        } else {
            return error(500, messageSource.getMessage("REQUEST_FAILED"));
        }
    }

    @RequiresPermissions("member:member-wallet:unlock-wallet")
    @PostMapping("unlock-wallet")
    @AccessLog(module = AdminModule.MEMBER, operation = "Unlock wallet")
    public MessageResult unlockWallet(Long uid, String unit) {
        memberWalletService.unlockWallet(uid, unit);
        return success(messageSource.getMessage("SUCCESS"));
    }

}
