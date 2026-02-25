package com.wikex.wikex.admin.core;

import com.wikex.wikex.admin.entity.Admin;
import com.wikex.wikex.admin.entity.AdminPermission;
import com.wikex.wikex.admin.service.AdminPermissionService;
import com.wikex.wikex.admin.service.AdminRoleService;
import com.wikex.wikex.admin.service.AdminService;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.service.LocaleMessageSourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Hevin
 * @date 2020-12-19
 */
@Slf4j
@Component(value = "realm")
public class AdminRealm extends AuthorizingRealm {

    @Resource
    private AdminRoleService sysRoleService;
    @Resource
    private AdminService adminService;
    @Resource
    private AdminPermissionService sysPermissionService;
    @Autowired
    private LocaleMessageSourceService messageSource;

    /**
     * Authorization
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String currentUsername = (String) getAvailablePrincipal(principals);

        List<String> permissionList = new ArrayList<>();
        Admin admin = (Admin) getSession(SysConstant.SESSION_ADMIN);
        if (null == admin) {
            throw new AuthorizationException();
        }
        try {
            List<AdminPermission> list;
            if ("root".equalsIgnoreCase(admin.getUsername())) {
                list = sysPermissionService.list();
            } else {
                list = sysPermissionService.getPermissionsByRid(admin.getRoleId());
            }
            list.forEach(x -> {
                if (!StringUtils.isEmpty(x.getName())) {
                    permissionList.add(x.getName());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthorizationException();
        }
        SimpleAuthorizationInfo simpleAuthorInfo = new SimpleAuthorizationInfo();
        simpleAuthorInfo.addStringPermissions(permissionList);
        return simpleAuthorInfo;
    }


    /**
     * Authentication
     *
     * @param authToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
        // Obtain token based on username and password
        UsernamePasswordToken token = (UsernamePasswordToken) authToken;
        String password = String.valueOf(token.getPassword());
        String username = token.getUsername();

        Admin admin = null;

        AuthenticationInfo authInfo = new SimpleAuthenticationInfo(token.getUsername(), token.getCredentials(), this.getName());
        try {
            admin = adminService.login(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (admin == null) {
            throw new AuthenticationException(messageSource.getMessage("USERNAME_OR_PASSWORD_INCORRECT"));
        } else if (admin.getEnable() == CommonStatus.ILLEGAL) {
            throw new AuthenticationException("This user has been disabled");
        } else {
            // Add login log
            adminService.update(new Date(), token.getHost(), admin.getId());
            setSession(SysConstant.SESSION_ADMIN, admin);
            return authInfo;
        }
    }

    /**
     * Store some data in ShiroSession for use elsewhere.
     * For example, in Controller, you can get it directly via HttpSession.getAttribute(key).
     *
     * @param key
     * @param value
     */
    private void setSession(Object key, Object value) {
        Subject currentUser = SecurityUtils.getSubject();
        if (null != currentUser) {
            Session session = currentUser.getSession();
            session.setTimeout(1800000L);

            if (null != session) {
                session.setAttribute(key, value);
            }
        }
    }

    private Object getSession(String key) {
        Subject currentUser = SecurityUtils.getSubject();
        if (null != currentUser) {
            return currentUser.getSession().getAttribute(key);
        }
        return null;
    }
}
