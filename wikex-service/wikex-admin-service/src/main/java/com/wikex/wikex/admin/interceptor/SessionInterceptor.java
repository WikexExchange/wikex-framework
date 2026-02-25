package com.wikex.wikex.admin.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.admin.service.AdminService;
import com.wikex.wikex.constant.SysConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


//@Component
public class SessionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SessionInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
        AdminService adminService = (AdminService) factory.getBean("adminServiceImpl");
        System.out.println(request.getContextPath());
        Subject currentUser = SecurityUtils.getSubject();

        // Determine if the user is automatically logged in through the "remember me" feature, while the session has expired
        if(!currentUser.isAuthenticated() && currentUser.isRemembered()){
            try {
                Admin admin = adminService.findByUsername(currentUser.getPrincipals().toString());
                // Verify after encrypting the password
                UsernamePasswordToken token = new UsernamePasswordToken(admin.getUsername(), admin.getPassword(), currentUser.isRemembered());
                // Put the current user into the session
                currentUser.login(token);
                Session session = currentUser.getSession();
                session.setAttribute(SysConstant.SESSION_ADMIN, admin);
                // Set the session expiration time -- ms, default is 30 minutes; negative value means never expires
                session.setTimeout(30*60*1000L);
            }catch (Exception e){
                // Auto-login failed, redirect to login page
                //response.sendRedirect(request.getContextPath()+"/system/employee/sign/in");
                ajaxReturn(response, 4000, "unauthorized");
                return false;
            }
            if(!currentUser.isAuthenticated()){
                // Auto-login failed, redirect to login page
                ajaxReturn(response, 4000, "unauthorized");
                return false;
            }
        }
        return true;
    }

    public void ajaxReturn(HttpServletResponse response, int code, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("message", msg);
        out.print(json.toString());
        out.flush();
        out.close();
    }

    public String getRemoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
