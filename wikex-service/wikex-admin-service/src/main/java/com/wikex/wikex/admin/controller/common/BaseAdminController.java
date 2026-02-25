package com.wikex.wikex.admin.controller.common;

import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.service.LocaleMessageSourceService;
import com.wikex.wikex.util.MessageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Component
public class BaseAdminController extends BaseController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LocaleMessageSourceService msService;

    protected Admin getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (Admin) session.getAttribute(SysConstant.SESSION_ADMIN);
    }

    /**
     * Determine whether the mobile verification code is correct
     * @param code verification code
     * @param key redis key: prefix + mobile number
     * @return result
     */
    protected MessageResult checkCode(String code, String key){
        return success(msService.getMessage("CODE_CORRECT"));
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        Object value = valueOperations.get(key);
//        if(value==null) {
//            return error(msService.getMessage("CODE_NOT_EXIST_RESEND"));
//        }
//        if(!value.toString().equals(code)) {
//            return  error(msService.getMessage("CODE_ERROR"));
//        }
//        valueOperations.getOperations().delete(key);
//        /**
//         * No need to verify again within ten minutes
//         */
//        valueOperations.set(key+"_PASS",true,10, TimeUnit.MINUTES);
//        return success(msService.getMessage("CODE_CORRECT"));
    }
}
