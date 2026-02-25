/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: DelegatingHawkFilterProxy.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-26
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-26, Create
 */
package com.wikex.wikex.netty.filter;

import com.wikex.wikex.core.annotation.HawkFilter;
import com.wikex.wikex.core.core.common.NettySpringContextUtils;
import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.core.exception.NettyException;
import com.wikex.wikex.core.filter.FilterChain;
import com.wikex.wikex.core.filter.HFilter;
import com.wikex.wikex.netty.shiro.filter.AbstractShiroFilter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * <p>
 * Title: DelegatingHawkFilterProxy
 * </p>
 * <p>
 * Description:
 * </p>
 * Filter proxy class, responsible for initializing the filter factory,
 * selecting different filters for business processing based on the request command.
 * All requests must go through this filter to obtain the corresponding session from the request.
 * If this filter is not passed, the corresponding session cannot be obtained.
 * 
 * @author MrGao
 * @date 2019-07-26
 */
@HawkFilter(order = 1)
public class DelegatingHawkFilterProxy extends HFilter {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext applicationContext;
	private String contextAttribute;
	private String targetBeanName;

	private Environment environment = new StandardEnvironment();
	private boolean targetFilterLifecycle = true;
	private final Object delegateMonitor = new Object();
	private volatile HFilter delegate;

	public DelegatingHawkFilterProxy() {
		init();
	}

	@Override
	public void doFilter(RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx, FilterChain chain)
			throws IOException, NettyException {
		doFilterInternal(request, response, ctx, chain);
	}

	public DelegatingHawkFilterProxy(HFilter delegate) {
		Assert.notNull(delegate, "delegate Filter object must not be null");
		this.delegate = delegate;
	}

	public DelegatingHawkFilterProxy(String targetBeanName) {
		this(targetBeanName, null);
	}

	public DelegatingHawkFilterProxy(String targetBeanName, ApplicationContext ac) {
		Assert.hasText(targetBeanName, "target Filter bean name must not be null or empty");
		this.setTargetBeanName(targetBeanName);
		this.applicationContext = ac;
		if (ac != null) {
			this.setEnvironment(ac.getEnvironment());
		}
	}

	//@PostConstruct
	@Override
	public void init() {
		initFilterBean();
		if (logger.isDebugEnabled()) {
			logger.debug("Filter configured successfully");
		}
	}

	protected void initFilterBean() throws NettyException {
		synchronized (this.delegateMonitor) {
			if (this.delegate == null) {
				// If no target bean name is specified, use the default filter name.
				if (this.targetBeanName == null) {
					this.targetBeanName = "hawkShiroFilter";
				}
				// Fetch the Spring root application context and initialize the delegate early if possible.
				// If the root application context will start after this filter proxy,
				// we will have to resort to lazy initialization.
				ApplicationContext wac = findApplicationContext();
				if (wac != null) {
					this.delegate = initDelegate(wac);
				}
			}
		}
	}

	protected HFilter initDelegate(ApplicationContext wac) throws NettyException {
		HFilter delegate = wac.getBean(getTargetBeanName(), AbstractShiroFilter.class);
		if (isTargetFilterLifecycle()) {
			delegate.init();
		}
		return delegate;
	}

	protected ApplicationContext findApplicationContext() {
		if (this.applicationContext != null) {
			// The user injected a context at construction time -> use it
			if (this.applicationContext instanceof ConfigurableApplicationContext) {
				ConfigurableApplicationContext cac = (ConfigurableApplicationContext) this.applicationContext;
				if (!cac.isActive()) {
					// The context has not been refreshed yet -> refresh it before returning
					cac.refresh();
				}
			}
			return this.applicationContext;
		}
		return NettySpringContextUtils.getApplicationContext();
	}

	public void doFilterInternal(RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx, FilterChain chain)
			throws NettyException, IOException {
		// Lazily initialize the delegate if necessary
		HFilter delegateToUse = this.delegate;
		if (delegateToUse == null) {
			synchronized (this.delegateMonitor) {
				if (this.delegate == null) {
					ApplicationContext wac = findApplicationContext();
					if (wac == null) {
						throw new IllegalStateException("No applicationContext found: "
								+ "no ContextLoaderListener or DispatcherServlet registered?");
					}
					this.delegate = initDelegate(wac);
				}
				delegateToUse = this.delegate;
			}
		}
		// Let the delegate execute the actual doFilter operation
		invokeDelegate(delegateToUse, request, response, ctx, chain);
	}

	protected void invokeDelegate(HFilter delegate, RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx, FilterChain chain)
			throws NettyException, IOException {
		delegate.doFilter(request, response, ctx, chain);
	}

	@Override
	public void destroy() {
		HFilter delegateToUse = this.delegate;
		if (delegateToUse != null) {
			destroyDelegate(delegateToUse);
		}
	}

	protected void destroyDelegate(HFilter delegate) {
		if (isTargetFilterLifecycle()) {
			delegate.destroy();
		}
	}

	public String getContextAttribute() {
		return contextAttribute;
	}

	public void setContextAttribute(String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public String getTargetBeanName() {
		return targetBeanName;
	}

	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public boolean isTargetFilterLifecycle() {
		return targetFilterLifecycle;
	}

	public void setTargetFilterLifecycle(boolean targetFilterLifecycle) {
		this.targetFilterLifecycle = targetFilterLifecycle;
	}

	public HFilter getDelegate() {
		return delegate;
	}

	public void setDelegate(HFilter delegate) {
		this.delegate = delegate;
	}

	public Object getDelegateMonitor() {
		return delegateMonitor;
	}
}
