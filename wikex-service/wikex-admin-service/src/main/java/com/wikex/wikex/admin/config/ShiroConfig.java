package com.wikex.wikex.admin.config;

import com.wikex.wikex.admin.core.AdminRealm;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
@Configuration
public class ShiroConfig {

    /**
     * ShiroFilterFactoryBean handles the interception of resource files.
     *
     * @param securityManager security manager
     * @return ShiroFilterFactoryBean
     */
    @Bean(name="shiroFilter")
    @DependsOn({"securityManager"})
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // Interceptor chain.
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/captcha", "anon");
        filterChainDefinitionMap.put("/admin/employee/login", "anon");
        filterChainDefinitionMap.put("/admin/code/**", "anon");
        filterChainDefinitionMap.put("admin/**/page-query", "user");
        filterChainDefinitionMap.put("/admin/employee/logout", "logout");
        filterChainDefinitionMap.put("admin/**/detail", "authc");
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        /*shiroFilterFactoryBean.setU("/403");*/
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }

    /**
     * Shiro lifecycle processor
     *
     *  /* 1. LifecycleBeanPostProcessor is a subclass of DestructionAwareBeanPostProcessor,
     *     responsible for initializing and destroying beans of type org.apache.shiro.util.Initializable.
     *     Mainly used by subclasses of AuthorizingRealm and by EhCacheManager.
     *
     * @return LifecycleBeanPostProcessor
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        LifecycleBeanPostProcessor lifecycleBeanPostProcessor = new LifecycleBeanPostProcessor();
        return lifecycleBeanPostProcessor;
    }

    /**
     * Shiro cache manager;
     * needs to be injected into related beans:
     * Security Manager: securityManager
     *
     * @return EhCacheManager
     */
    @Bean(name="ehCacheManager")
    @DependsOn("lifecycleBeanPostProcessor")
    public EhCacheManager ehCacheManager() {
        
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return cacheManager;
    }

    @Bean(name="adminRealm")
    @DependsOn("lifecycleBeanPostProcessor")
    public AdminRealm adminRealm(EhCacheManager ehCacheManager) {
        AdminRealm adminRealm = new AdminRealm() ;
        // To ensure password security, you can define a hash algorithm (no hash is applied here; plain password matching).
        /*HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName("SHA-1");
        matcher.setHashIterations(2);
        matcher.setStoredCredentialsHexEncoded(true);
        adminRealm.setCredentialsMatcher(matcher);*/
        adminRealm.setCacheManager(ehCacheManager);
        return adminRealm;
    }

    /**
     * Set rememberMe Cookie for 7 days
     * @return SimpleCookie
     */
    @Bean(name="simpleCookie")
    public SimpleCookie getSimpleCookie(){
        SimpleCookie simpleCookie = new SimpleCookie();
        simpleCookie.setName("rememberMe");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setMaxAge(7*24*60*60);
        return simpleCookie ;
    }

    /**
     * Cookie manager
     * @return CookieRememberMeManager
     */
    @Bean(name="cookieRememberMeManager")
    @DependsOn({"simpleCookie"})
    public CookieRememberMeManager getCookieRememberMeManager(SimpleCookie simpleCookie){
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(simpleCookie);
        /**
         * Set the key for rememberMe cookie; if not set, it's likely to throw:
         * javax.crypto.BadPaddingException: Given final block not properly padded
         */
        cookieRememberMeManager.setCipherKey(Base64.decode("2AvVhdsgUs0FSA3SDFAdag=="));
        return cookieRememberMeManager ;
    }

    /**
     * @DependsOn: Before initializing the DefaultWebSecurityManager instance,
     *             force initialization of adminRealm, ehCacheManager, cookieRememberMeManager, etc.
     * @param realm AdminRealm
     * @param ehCacheManager EhCacheManager
     * @param cookieRememberMeManager CookieRememberMeManager
     * @return DefaultWebSecurityManager
     */
    @Bean(name = "securityManager")
    @DependsOn({"adminRealm","ehCacheManager","cookieRememberMeManager"})
    public DefaultWebSecurityManager getDefaultWebSecurityManager(AdminRealm realm, EhCacheManager ehCacheManager,CookieRememberMeManager cookieRememberMeManager) {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        // Set realm.
        defaultWebSecurityManager.setRealm(realm);
        defaultWebSecurityManager.setCacheManager(ehCacheManager);
        defaultWebSecurityManager.setSessionManager(sessionManager());
        defaultWebSecurityManager.setRememberMeManager(cookieRememberMeManager);
        return defaultWebSecurityManager;
    }

    @Bean
    public ShiroSession sessionManager(){
        ShiroSession shiroSession = new ShiroSession();
        shiroSession.setSessionDAO(new EnterpriseCacheSessionDAO());
        return shiroSession;
    }

    /**
     * Enable Shiro annotations (such as @RequiresRoles, @RequiresPermissions).
     * Requires Spring AOP to scan classes using Shiro annotations and perform security logic when necessary.
     * Configure the following two beans
     * (DefaultAdvisorAutoProxyCreator (optional) and AuthorizationAttributeSourceAdvisor)
     * to enable this functionality.
     */

    /**
     * The Advisor decides which class methods will be proxied by AOP.
     * @return DefaultAdvisorAutoProxyCreator
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAAP = new DefaultAdvisorAutoProxyCreator();
        defaultAAP.setProxyTargetClass(true);
        return defaultAAP;
    }

    @Bean
    @DependsOn("securityManager")
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }
}
