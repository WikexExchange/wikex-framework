/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkNettyConfiguration.java</p>
 * 
 * Description: 
 * @version 1.0
 * History:
 * v1.0.0, , 2019-12-20, Create
 */
package com.wikex.wikex;

import com.wikex.wikex.core.configuration.NettyProperties;
import com.wikex.wikex.core.context.HawkContext;
import com.wikex.wikex.core.core.common.NettySpringContextUtils;
import com.wikex.wikex.netty.codec.DefaultCodec;
import com.wikex.wikex.netty.dispatcher.HawkRequestDispatcher;
import com.wikex.wikex.netty.push.HawkPushServiceApi;
import com.wikex.wikex.netty.push.impl.HawkPushServiceImpl;
import com.wikex.wikex.netty.server.*;
import com.wikex.wikex.netty.shiro.HawkShiroFilterFactoryBean;
import com.wikex.wikex.netty.shiro.SequenceSessionIdGenerator;
import com.wikex.wikex.netty.shiro.cache.SpringCacheManagerWrapper;
import com.wikex.wikex.netty.shiro.mgt.DefaultHawkSecurityManager;
import com.wikex.wikex.netty.shiro.mgt.DefaultHawkSubjectFactory;
import com.wikex.wikex.netty.shiro.realm.HawkServerRealm;
import com.wikex.wikex.netty.shiro.session.DefaultHawkSessionManager;
import com.wikex.wikex.service.ChannelEventDealService;
import com.wikex.wikex.service.DefaultChannelEventDealService;
import com.wikex.wikex.service.DefaultLoginUserService;
import com.wikex.wikex.service.LoginUserService;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Title: HawkNettyConfiguration</p>
 * <p>Description: </p>
 */
@Configuration
@EnableCaching
public class HawkNettyConfiguration {

	@Bean
	public NettyProperties nettyProperties(){
		return new NettyProperties();
	}
	/**
	 *
	 * <p>Title: hawkServerRealm</p>
	 * <p>Description: </p>
	 * The following section is all related to Shiro configuration
	 * @return realm
	 */
	@Bean
	public HawkServerRealm hawkServerRealm() {
		return new HawkServerRealm();
	}

	@Bean
	public SequenceSessionIdGenerator sessionIdGenerator() {
		return new SequenceSessionIdGenerator();
	}

	@Bean
	public EnterpriseCacheSessionDAO sessionDAO(SequenceSessionIdGenerator sessionIdGenerator) {
		EnterpriseCacheSessionDAO sessionDAO = new EnterpriseCacheSessionDAO();
		sessionDAO.setActiveSessionsCacheName("shiro-activeSessionCache");
		sessionDAO.setSessionIdGenerator(sessionIdGenerator);
		return sessionDAO;
	}

	@Bean
	public QuartzSessionValidationScheduler sessionValidationScheduler() {
		QuartzSessionValidationScheduler sessionValidationScheduler = new QuartzSessionValidationScheduler();
		sessionValidationScheduler.setSessionValidationInterval(1800000);
		return sessionValidationScheduler;
	}

	@Bean
	public DefaultHawkSessionManager sessionManager(CachingSessionDAO sessionDAO) {
		DefaultHawkSessionManager defaultSessionManager = new DefaultHawkSessionManager();
		defaultSessionManager.setGlobalSessionTimeout(1800000);
		defaultSessionManager.setDeleteInvalidSessions(true);
//		defaultSessionManager.setSessionValidationSchedulerEnabled(true);
//		defaultSessionManager.setSessionValidationScheduler(sessionValidationScheduler);
		defaultSessionManager.setSessionDAO(sessionDAO);
		return defaultSessionManager;
	}

	@Bean
	public DefaultHawkSubjectFactory hawkSubjectFactory() {
		return new DefaultHawkSubjectFactory();
	}

	@Bean
	public DefaultHawkSecurityManager securityManager(HawkServerRealm hawkServerRealm,
													  DefaultHawkSessionManager sessionManager, SpringCacheManagerWrapper springCacheManagerWrapper,
													  DefaultHawkSubjectFactory hawkSubjectFactory) {
		DefaultHawkSecurityManager securityManager = new DefaultHawkSecurityManager();
		securityManager.setRealm(hawkServerRealm);
		securityManager.setSessionManager(sessionManager);
		securityManager.setCacheManager(springCacheManagerWrapper);
		securityManager.setSubjectFactory(hawkSubjectFactory);
		return securityManager;
	}

	@Bean
	public MethodInvokingFactoryBean methodInvokingFactoryBean(DefaultHawkSecurityManager securityManager) {
		MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
		methodInvokingFactoryBean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
		methodInvokingFactoryBean.setArguments(securityManager);
		return methodInvokingFactoryBean;
	}

	@Bean
	public HawkShiroFilterFactoryBean hawkShiroFilter(DefaultHawkSecurityManager securityManager) {
		HawkShiroFilterFactoryBean hawkShiroFilter = new HawkShiroFilterFactoryBean();
		hawkShiroFilter.setSecurityManager(securityManager);
		return hawkShiroFilter;
	}

	@Bean
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 *
	 * <p>Title: codec</p>
	 * <p>Description: </p>
	 * Message decoding method, default is no decoding
	 * @return decoder
	 */
	@Bean
	public DefaultCodec codec(){
		return new DefaultCodec();
	}
	/**
	 *
	 * <p>Title: dispatcher</p>
	 * <p>Description: </p>
	 * Request dispatch service
	 * @return dispatcher
	 */
	@Bean
	public HawkRequestDispatcher dispatcher(){
		return  new HawkRequestDispatcher();
	}

	/**
	 *
	 * @param nettyProperties netty configuration
	 * @return request thread dispatcher
	 */
	@Bean
	public HandlerThreadDispatcher threadDispatcher(NettyProperties nettyProperties){
		return new HandlerThreadDispatcher(nettyProperties);
	}
	/**
	 *
	 * <p>Title: hawkContext</p>
	 * <p>Description: </p>
	 * Netty request handling annotation context
	 * @return context object
	 */
	@Bean
	public HawkContext hawkContext(){
		return new HawkContext();
	}
	/**
	 *
	 * <p>Title: hawkServerRealHandler</p>
	 * <p>Description: </p>
	 * Netty handler
	 * @return real netty handler
	 */
	@Bean
	@ConditionalOnMissingBean(HawkServerHandler.class)
	public HawkServerRealHandler hawkServerRealHandler(){
		return new HawkServerRealHandler();
	}
	/**
	 *
	 * <p>Title: hawkServerInitializer</p>
	 * <p>Description: </p>
	 * Netty initialization configuration
	 * @return server initializer object
	 */
//	@Bean
//	public ChannelInitializer<SocketChannel> hawkServerInitializer(){
//		return new HawkServerInitializer();
//	}
	/**
	 *
	 * <p>Title: hawkServerInitializer</p>
	 * <p>Description: </p>
	 * Netty initialization configuration
	 * @return server initializer object
	 */
//	@Bean
//	public ChannelInitializer<SocketChannel> webSocketChannelInitializer(){
//		return new WebSocketChannelInitializer();
//	}
	/**
	 *
	 * <p>Title: nettyServer</p>
	 * <p>Description: </p>
	 * Netty startup application
	 * @return netty service
	 */
	@Bean
	public NettyApplicationStartup nettyApplicationStartup(){
		return new NettyApplicationStartup();
	}
	/**
	 *
	 * <p>Title: loginUserService</p>
	 * <p>Description: </p>
	 * Use default method when no login entity method exists
	 * @return login service
	 */
	@Bean
	@ConditionalOnMissingBean(LoginUserService.class)
	public LoginUserService loginUserService(){
		return new DefaultLoginUserService();
	}


	@Bean
	@ConditionalOnMissingBean(ChannelEventDealService.class)
	public ChannelEventDealService channelEventDealService(){
		return new DefaultChannelEventDealService();
	}
	@Bean
	public NettySpringContextUtils nettySpringContextUtils(){
		return  new NettySpringContextUtils();
	}

	@Bean
	public HawkPushServiceApi hawkPushServiceApi(){
		return new HawkPushServiceImpl();
	}
//	@Bean
//	public LoginHandler loginHandler(){
//		return  new LoginHandler();
//	}
//	@Bean
//	public HeartBeatHandler heartBeatHandler(){
//		return  new HeartBeatHandler();
//	}
//	@Bean
//	public AccessAuthFilter accessAuthFilter(){
//		return  new AccessAuthFilter();
//	}
//	@Bean
//	public DelegatingHawkFilterProxy delegatingHawkFilterProxy(){
//		return new DelegatingHawkFilterProxy();
//	}
}
