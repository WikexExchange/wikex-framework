package com.wikex.wikex.listener;

import com.wikex.wikex.annotation.PermissionOperation;
import com.wikex.wikex.permission.entity.Permission;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

@Component
public class PermissionListener implements BeanPostProcessor {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        if(methods != null){
            for (Method method : methods) {
                PermissionOperation operation = AnnotationUtils.findAnnotation(method,PermissionOperation.class);
                if(operation != null){
                    RequestMapping mapping = AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequestMapping.class);
                    String baseUrl = "/";
                    if(mapping!=null && mapping.value().length>0) {
                       baseUrl = mapping.value()[0];
                        if(!baseUrl.endsWith("/")){
                            baseUrl += "/";
                        }
                        if(!baseUrl.startsWith("/")){
                            baseUrl = "/" + baseUrl;
                        }
                    }

                    RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
                    if(requestMapping!=null) {
                        for (String s : requestMapping.value()) {
                            if(s.startsWith("/")){
                                s = s.replaceFirst("/","");
                            }
                            savePermission("*", baseUrl + s);
                        }
                    }
                    PostMapping  postMapping = AnnotationUtils.findAnnotation(method, PostMapping.class);
                    if(postMapping!=null) {
                        for (String s : postMapping.value()) {
                            if(s.startsWith("/")){
                                s = s.replaceFirst("/","");
                            }
                            savePermission("POST", baseUrl + s);
                        }
                    }
                    GetMapping  getMapping = AnnotationUtils.findAnnotation(method, GetMapping.class);
                    if(getMapping!=null) {
                        for (String s : getMapping.value()) {
                            if(s.startsWith("/")){
                                s = s.replaceFirst("/","");
                            }
                            savePermission( "GET", baseUrl + s);
                        }
                    }
                }
            }
        }

        return bean;
    }

    private void savePermission(String methodString, String url) {
        Integer urlMatch = 0;
        if(url.indexOf("{")>0){
            urlMatch = 1;
        }
        Permission permission = new Permission();
        permission.setMethod(methodString);
        permission.setServiceName(serviceName);
        permission.setSourceName("");
        permission.setUrl(url);
        redisTemplate.boundHashOps("permission:"+urlMatch).put(serviceName+url,permission);
    }

}
