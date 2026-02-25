package com.wikex.wikex.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.admin.controller.common.BaseAdminController;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.admin.entity.AdminRole;
import com.wikex.wikex.admin.entity.Department;
import com.wikex.wikex.admin.service.AdminPermissionService;
import com.wikex.wikex.admin.service.AdminRoleService;
import com.wikex.wikex.admin.service.AdminService;
import com.wikex.wikex.admin.service.DepartmentService;
import com.wikex.wikex.annotation.AccessLog;
import com.wikex.wikex.constant.AdminModule;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.core.Menu;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.sms.SMSProvider;
import com.wikex.wikex.util.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;




@Slf4j
@Controller
@RequestMapping("/system/employee")
public class EmployeeController extends BaseAdminController {

    @Value("${system.md5.key}")
    private String md5Key;

    @Autowired
    private AdminRoleService adminRoleService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private DepartmentService departmentService;
    @Resource
    private AdminPermissionService adminPermissionService;

    @Autowired
    private RedisTemplate redisTemplate ;

    @Autowired
    private SMSProvider smsProvider ;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private LocaleMessageSourceService messageSource;

    @Value("${spring.mail.username}")
    private String from;
    @Value("${spark.system.host}")
    private String host;
    @Value("${spark.system.name}")
    private String company;

    @Value("${spark.system.admins}")
    private String admins;


    @RequestMapping(value = "/login")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "Check backend login input with mobile verification code")
    public MessageResult adminLogin(@RequestParam(value = "username",required = true)String username,
                                    @RequestParam(value = "password",required = true)String password,
                                    @RequestParam(value = "captcha",required = false)String captcha) throws Exception {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("Username or password cannot be empty");
        }
        String ADMIN_LOGIN = "ADMIN_LOGIN";
        
        password = MD5.md5(password + md5Key);
        
        Admin admin = adminService.login(username,password);
        if(admin==null){
            return error(messageSource.getMessage("USERNAME_OR_PASSWORD_NOT_FOUND"));
        }else{
            try {
                UsernamePasswordToken token = new UsernamePasswordToken(username, password,true);
                SecurityUtils.getSubject().login(token);
                List<Menu> list;
                if ("root".equalsIgnoreCase(admin.getUsername())) {
                    list = adminRoleService.toMenus(adminPermissionService.list(), 0L);
                } else {
                    list = adminRoleService.toMenus(adminPermissionService.getPermissionsByRid(admin.getRoleId()), 0L);
                }
                Subject subject = SecurityUtils.getSubject();
                Serializable tokenId = subject.getSession().getId();
                Map<String, Object> map = new HashMap<>();
                map.put("authToken", tokenId);
                map.put("permissions", list);
                map.put("admin", admin);
                String[] adminList = admins.split(",");
                for(int i = 0; i < adminList.length; i++) {
                    sendEmailMsg(adminList[i], "Administrator (UserName: " + username + ", Phone: " + admin.getMobilePhone()+ ") logged into the backend", "Admin Login Notification");
                }
                return success(messageSource.getMessage("LOGIN_SUCCESS"), map);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return error(messageSource.getMessage("FAILED_TO_SEND_OR_SAVE_MOBILE_CODE"));
        }
    }

    /**
     * Submit login information
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "sign/in")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "Submit login information Admin")
    public MessageResult doLogin(@SessionAttribute("username")String username,
                                 @SessionAttribute("password")String password,
                                 @SessionAttribute("phone")String phone, String code,
                                 @RequestParam(value="rememberMe",defaultValue = "true")boolean rememberMe,
                                 HttpServletRequest request) {
    	/*
        Assert.notNull(code,"Please enter verification code");
        Assert.isTrue(StringUtils.isNotEmpty(username)&&StringUtils.isNotEmpty(password)&&StringUtils.isNotEmpty(phone),"Session expired");
        ValueOperations valueOperations = redisTemplate.opsForValue() ;
        Object cacheCode = valueOperations.get(SysConstant.ADMIN_LOGIN_PHONE_PREFIX+phone);
        Assert.notNull(cacheCode,"Verification code has been cleared, please resend");
        if (!code.equals(cacheCode.toString())) {
            return error("Mobile verification code is incorrect, please re-enter");
        }
        */
        try {
            

            UsernamePasswordToken token = new UsernamePasswordToken(username, password,true);
            token.setHost(IPUtils.getIpAddr(request));
            SecurityUtils.getSubject().login(token);
            //valueOperations.getOperations().delete(SysConstant.ADMIN_LOGIN_PHONE_PREFIX+phone);
            Admin admin = (Admin) request.getSession().getAttribute(SysConstant.SESSION_ADMIN);
            token.setRememberMe(true);

            // Get current user's menu permissions
            List<Menu> list;
            if ("root".equalsIgnoreCase(admin.getUsername())) {
                list = adminRoleService.toMenus(adminPermissionService.list(), 0L);
            } else {
                list = adminRoleService.toMenus(adminPermissionService.getPermissionsByRid(admin.getRoleId()), 0L);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("permissions", list);
            map.put("admin", admin);
            map.put("authToken",request.getSession().getId());
            String[] adminList = admins.split(",");
			for(int i = 0; i < adminList.length; i++) {
				sendEmailMsg(adminList[i], "Administrator (UserName: " + username + ", Phone: " + phone+ ") logged into the backend", "Admin Login Notification");
			}

            return success(messageSource.getMessage("LOGIN_SUCCESS"), map);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return error(e.getMessage());
        }
    }



    /**
     * Send Email
     * @param email
     * @param msg
     * @param subject
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

    @RequestMapping(value = "/check")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "Check backend login input with mobile verification code")
    public MessageResult valiatePhoneCode(HttpServletRequest request){
        String username = Convert.strToStr(request(request, "username"), "");
        String password = Convert.strToStr(request(request, "password"), "");
        String captcha = Convert.strToStr(request(request, "captcha"), "");
        String sid = Convert.strToStr(request(request, "sid"), "");
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error(messageSource.getMessage("USERNAME_OR_PASSWORD_CANNOT_BE_EMPTY"));
        }
        HttpSession session = request.getSession();
        request.getSession().setAttribute("test", "123456789");
        
        if (StringUtils.isBlank(captcha)) {
            return error(messageSource.getMessage("VERIFICATION_CODE_CANNOT_BE_EMPTY"));
        }
        String ADMIN_LOGIN = "ADMIN_LOGIN";
        

        // Store the verification code in redis with a page key
        String key = "CAPTCHA_" + ADMIN_LOGIN + sid;
        ValueOperations<String,String> opt = redisTemplate.opsForValue();
        String sss = opt.get(key);
        redisTemplate.expire(key,1, TimeUnit.SECONDS);
        

        if (!captcha.equalsIgnoreCase(sss)) {
            return error(messageSource.getMessage("INVALID_VERIFICATION_CODE"));
        }
        try {
            password = MD5.md5(password + md5Key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Admin admin = adminService.login(username,password);
        if(admin==null){
            return error(messageSource.getMessage("USERNAME_OR_PASSWORD_NOT_FOUND"));
        }else{
            try {
                request.getSession().setAttribute("username",username);
                request.getSession().setAttribute("password",password);
                request.getSession().setAttribute("phone",admin.getMobilePhone());
                Map<String,String> result = new HashMap<>();
                result.put("mobile",admin.getMobilePhone());
                result.put("authToken",request.getSession().getId());
               return success("",result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return error(messageSource.getMessage("FAILED_TO_SEND_OR_SAVE_MOBILE_CODE"));
        }
    }


/**
     * Logout
     *
     * @return
     */
    @RequestMapping(value = "logout")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "Logout")
    public MessageResult logout() {
        SecurityUtils.getSubject().logout();
        return success();
    }

/**
     * Create or update backend user
     *
     * @param admin
     * @param bindingResult
     * @return
     */


    @RequiresPermissions("system:employee:merge")
    @RequestMapping(value = "/merge")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "Create or update backend user")
    @Transactional(rollbackFor = Exception.class)
    public MessageResult addAdmin(Admin admin,
                                  @RequestParam("departmentId") Long departmentId,
                                  BindingResult bindingResult) throws Exception {
        MessageResult result = BindingResultUtil.validate(bindingResult);
        if (result != null) {
            return result;
        }
        Assert.notNull(departmentId, messageSource.getMessage("PLEASE_SELECT_DEPARTMENT"));
        Department department = departmentService.getById(departmentId);
        admin.setDepartmentId(department.getId());
        String password;
        if (admin.getId() != null) {
            Admin admin1 = adminService.getById(admin.getId());
            admin.setLastLoginIp(admin1.getLastLoginIp());
            admin.setLastLoginTime(admin1.getLastLoginTime());
            // If password is not null, change the password
            if (StringUtils.isNotBlank(admin.getPassword())) {
                password = MD5.md5(admin.getPassword() + md5Key);
            } else {
                password = admin1.getPassword();
            }
        } else {
            // This is a new addition
            Admin a = adminService.findByUsername(admin.getUsername());
            if (a != null) {
                return error(messageSource.getMessage("USERNAME_ALREADY_EXISTS"));
            }
            if (StringUtils.isBlank(admin.getPassword())) {
                return error(messageSource.getMessage("PASSWORD_CANNOT_BE_EMPTY"));
            }
            password = MD5.md5(admin.getPassword() + md5Key);
        }
        admin.setPassword(password);
        adminService.saveOrUpdate(admin);
        return success(messageSource.getMessage("OPERATION_SUCCESS"));
    }

    @ResponseBody
    @RequiresPermissions("system:employee:page-query")
    @PostMapping("page-query")
    @AccessLog(module = AdminModule.SYSTEM, operation = "Paginated query backend users admin")
    public MessageResult findAllAdminUser(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "searchKey", defaultValue = "") String searchKey) {
        QueryWrapper<Admin> query = new QueryWrapper<>();
        query.ne("username","root")
                .and(
                        wrapper -> wrapper.like("email",searchKey).or()
                        .like("real_name",searchKey).or()
                        .like("mobile_phone",searchKey).or()
                        .like("username",searchKey)
                ).orderByDesc("id");
        IPage<Admin> page = new Page<>(pageNo,pageSize);
        IPage<Admin> all = adminService.page(page, query);
        for (Admin admin : all.getRecords()) {
            AdminRole role = adminRoleService.getById(admin.getRoleId());
            admin.setRoleName(role.getRole());
            Department department = departmentService.getById(admin.getDepartmentId());
            admin.setDepartmentName(department.getName());
        }
        return success(IPage2Page(all));
    }

    @RequiresPermissions("system:employee:update-password")
    @PostMapping("update-password")
    @ResponseBody
    public MessageResult updatePassword(Long id, String lastPassword, String newPassword) throws Exception {
        Assert.notNull(id, "admin id cannot be null");
        Assert.notNull(lastPassword, messageSource.getMessage("ENTER_ORIGINAL_PASSWORD"));
        Assert.notNull(newPassword, messageSource.getMessage("ENTER_NEW_PASSWORD"));
        Admin admin = adminService.getById(id);
        lastPassword = MD5.md5(lastPassword + md5Key);
        Assert.isTrue(lastPassword.equalsIgnoreCase(admin.getPassword()), messageSource.getMessage("PASSWORD_INCORRECT"));
        admin.setPassword(MD5.md5(newPassword + md5Key));
        adminService.saveOrUpdate(admin);
        return MessageResult.success(messageSource.getMessage("CHANGE_PASSWORD_SUCCESS"));
    }

/**
     * Admin information
     *
     * @param id
     * @return
     */


    @RequiresPermissions("system:employee:detail")
    @RequestMapping(value = "/detail")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "Backend user Admin details")
    public MessageResult adminDetail(Long id) {
        Map map = adminService.findAdminDetail(id);
        MessageResult result = success();
        result.setData(map);
        return result;
    }



/**
     * Admin information
     *
     * @return
     */


    @RequiresPermissions("system:employee:deletes")
    @RequestMapping(value = "/deletes")
    @ResponseBody
    @AccessLog(module = AdminModule.SYSTEM, operation = "Backend user Admin delete")
    public MessageResult deletes(Long[] ids) {
        adminService.deletes(ids);
        return MessageResult.success(messageSource.getMessage("BATCH_DELETE_SUCCESS"));
    }
}
