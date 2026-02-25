package com.wikex.wikex.core.context;

import com.wikex.wikex.core.annotation.*;
import com.wikex.wikex.core.exception.NettyException;
import com.wikex.wikex.core.filter.HFilter;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * <p>Title: HawkContext</p>
 * <p>Description:</p>
 * Executes on top of Spring container object initialization,
 * allowing execution before or after object initialization.
 * <ul>
 *     <li>Checks whether the class of the object is annotated with {@link HawkBean};</li>
 *     <li>If annotated with {@link HawkBean}, retrieves all methods annotated with {@link HawkMethod}.</li>
 * </ul>
 * @author MrGao
 * @date July 18, 2019
 */
public class HawkContext implements BeanPostProcessor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, HawkMethodHandler> hawkMethodHandlerMap;
    private TreeSet<HawkFilterValue> filters;

    public HawkContext() {
        this.hawkMethodHandlerMap = new HashMap<>();
        this.filters = new TreeSet<>();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (AnnotationUtils.findAnnotation(bean.getClass(), HawkBean.class) != null) {
            ReflectionUtils.doWithMethods(bean.getClass(),
                    method -> {

                        // Annotation for HawkBean method
                        HawkMethod hawkMethod = AnnotationUtils.findAnnotation(method, HawkMethod.class);
                        // Values from method annotation
                        HawkMethodValue HawkMethodValue = new HawkMethodValue(hawkMethod.cmd(), hawkMethod.version(),
                                ObsoletedType.isObsoleted(hawkMethod.obsoleted()));
                        // Class handling the method
                        HawkMethodHandler HawkMethodHandler = new HawkMethodHandler();
                        // Service method value
                        HawkMethodHandler.setHawkMethodValue(HawkMethodValue);
                        // Handler
                        HawkMethodHandler.setHandler(bean);
                        // Method
                        HawkMethodHandler.setHandlerMethod(method);
                        String handlerKey = buildeHandlerKey(HawkMethodValue.getCmd(), HawkMethodValue.getVersion());
                        // Check for duplicates
                        if (hawkMethodHandlerMap.get(handlerKey) != null) {
                            throw new NettyException(
                                    new StringBuilder("Duplicate command, ").append(handlerKey).toString());
                        }
                        // Check return type
                        if (!ClassUtils.isAssignable(MessageLite.class, method.getReturnType())) {
                            throw new NettyException("Return type can only be MessageLite or its subclasses");
                        }
                        if (method.getParameterTypes().length > 3) {
                            throw new NettyException(String.format("%s#%s can contain at most three parameters",
                                    method.getDeclaringClass().getCanonicalName(), method.getName()));
                        } else if (method.getParameterTypes().length == 1) {
                            if (!(ClassUtils.isAssignable(long.class, method.getParameterTypes()[0])
                                    || ClassUtils.isAssignable(byte[].class, method.getParameterTypes()[0])
                                    || ClassUtils.isAssignable(ChannelHandlerContext.class, method.getParameterTypes()[0]))) {
                                throw new NettyException(String.format("Allowed: %s#%s(long), (byte[]) or (ChannelHandlerContext)",
                                        method.getDeclaringClass().getCanonicalName(), method.getName()));
                            }
                        } else if (method.getParameterTypes().length == 2) {
                            boolean fail = true;
                            if (ClassUtils.isAssignable(long.class, method.getParameterTypes()[0])
                                    && (ClassUtils.isAssignable(byte[].class, method.getParameterTypes()[1])
                                    || ClassUtils.isAssignable(ChannelHandlerContext.class, method.getParameterTypes()[1]))) {
                                fail = false;
                            }
                            if (ClassUtils.isAssignable(byte[].class, method.getParameterTypes()[0])
                                    && (ClassUtils.isAssignable(long.class, method.getParameterTypes()[1])
                                    || ClassUtils.isAssignable(ChannelHandlerContext.class, method.getParameterTypes()[1]))) {
                                fail = false;
                            }
                            if (ClassUtils.isAssignable(ChannelHandlerContext.class, method.getParameterTypes()[0])
                                    && (ClassUtils.isAssignable(long.class, method.getParameterTypes()[1])
                                    || ClassUtils.isAssignable(byte[].class, method.getParameterTypes()[1]))) {
                                fail = false;
                            }
                            if (fail) {
                                throw new NettyException(String.format(
                                        "Allowed: %s#%s(long, byte[]) or (long, ChannelHandlerContext), " +
                                        "(byte[], long) or (byte[], ChannelHandlerContext), " +
                                        "(ChannelHandlerContext, long) or (ChannelHandlerContext, byte[])",
                                        method.getDeclaringClass().getCanonicalName(), method.getName()));
                            }
                        } else if (method.getParameterTypes().length == 3) {
                            if (!ClassUtils.isAssignable(long.class, method.getParameterTypes()[0])
                                    || !ClassUtils.isAssignable(byte[].class, method.getParameterTypes()[1])
                                    || !ClassUtils.isAssignable(ChannelHandlerContext.class, method.getParameterTypes()[2])) {
                                throw new NettyException(String.format("Allowed: %s#%s(long, byte[], ChannelHandlerContext)",
                                        method.getDeclaringClass().getCanonicalName(), method.getName()));
                            }
                        }
                        hawkMethodHandlerMap.put(handlerKey, HawkMethodHandler);
                        logger.info(String.format("Registered command %s", handlerKey));
                    },
                    method -> !method.isSynthetic() && AnnotationUtils.findAnnotation(method, HawkMethod.class) != null
            );
        }
        // Scan filters
        HawkFilter HawkFilter = AnnotationUtils.findAnnotation(bean.getClass(), HawkFilter.class);
        if (HawkFilter != null) {
            logger.info(String.format("Add filter %s", bean.getClass()));
            this.filters.add(new HawkFilterValue(HawkFilter.order(), HawkFilter.cmds(), HawkFilter.ignoreCmds(), (HFilter) bean));
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public HawkMethodHandler getHawkMethodHandler(int cmd, int version) {
        String handlerKey = buildeHandlerKey(cmd, version);
        HawkMethodHandler handler = hawkMethodHandlerMap.get(handlerKey);
        return handler;
    }

    public TreeSet<HawkFilterValue> getFilters() {
        return filters;
    }

    private String buildeHandlerKey(int cmd, int version) {
        return new StringBuilder().append(cmd).append("#").append(version).toString();
    }
}
