package com.wikex.wikex.user.aspect;

import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.service.LocaleMessageSourceService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;


@Aspect
@Component
@Slf4j
public class AntiAttackAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private LocaleMessageSourceService localeMessageSourceService;

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut("execution(public * com.wikex.wikex.user.controller.RegisterController.sendBindEmail(..))" +
            "||execution(public * com.wikex.wikex.user.controller.RegisterController.sendAddAddress(..))" +
            "||execution(public * com.wikex.wikex.user.controller.SmsController.sendResetTransactionCode(..))" +
            "||execution(public * com.wikex.wikex.user.controller.SmsController.setBindPhoneCode(..))" +
            "||execution(public * com.wikex.wikex.user.controller.SmsController.updatePasswordCode(..))" +
            "||execution(public * com.wikex.wikex.user.controller.SmsController.addAddressCode(..))" +
            "||execution(public * com.wikex.wikex.user.controller.SmsController.resetPhoneCode(..))")
    public void antiAttack() {
    }

    @Before("antiAttack()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        
        check(joinPoint);
    }

    public void check(JoinPoint joinPoint) throws Exception {
        startTime.set(System.currentTimeMillis());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId();
        Object code = valueOperations.get(key);
        if (code != null) {
            throw new IllegalArgumentException(localeMessageSourceService.getMessage("FREQUENTLY_REQUEST"));
        }
    }

    @AfterReturning(pointcut = "antiAttack()")
    public void doAfterReturning() throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String key = SysConstant.ANTI_ATTACK_ + request.getSession().getId();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, "send sms all too often", 1, TimeUnit.MINUTES);
        startTime.remove();
    }
}
