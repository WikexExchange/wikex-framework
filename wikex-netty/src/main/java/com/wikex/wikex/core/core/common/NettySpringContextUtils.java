
package com.wikex.wikex.core.core.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>Title: SpringContextUtils</p>
 * <p>Description: </p>
 * <p>Spring utility class used to get beans from the container</p>
 * @author fanqiong
 * @date 
 */
public class NettySpringContextUtils implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext = null;  

	/* (non-Javadoc)
	 * <p>Title: setApplicationContext</p>
	 * <p>Description: </p>
	 * @param applicationContext
	 * @throws BeansException
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 * When extending the ApplicationContextAware interface, this method will be automatically called 
	 * when the program calls getBean(String), no need to call it manually.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        NettySpringContextUtils.applicationContext = applicationContext;
	}
	
	public static ApplicationContext getApplicationContext() {  
        return applicationContext;  
    }  

    /*** 
     * Get the corresponding bean from the configuration file by its bean ID.
     * @param name 
     * @return 
     * @throws BeansException 
     */  
    public static Object getBean(String name) throws BeansException {  
        return applicationContext.getBean(name);  
    } 
    
    /*** 
     * Similar to getBean(String name) but provides the required return type in the parameter.
     * @param name 
     * @param requiredType 
     * @return 
     * @throws BeansException 
     */  
	public static Object getBean(String name, Class requiredType) throws BeansException {  
        return applicationContext.getBean(name, requiredType);  
    }  
           
    /** 
     * If the BeanFactory contains a bean definition matching the given name, return true.
     * @param name 
     * @return boolean 
     */  
    public static boolean containsBean(String name) {  
         return applicationContext.containsBean(name);  
    }  
           
    /** 
     * Determine whether the bean definition registered with the given name is a singleton or a prototype. 
     * If no bean definition is found for the given name, a NoSuchBeanDefinitionException will be thrown.
     * @param name 
     * @return boolean 
     * @throws NoSuchBeanDefinitionException 
     */  
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {  
          return applicationContext.isSingleton(name);  
    }  
           
    /** 
     * @param name 
     * @return Class - the type of the registered object 
     * @throws NoSuchBeanDefinitionException 
     */  
	public static Class getType(String name) throws NoSuchBeanDefinitionException {  
         return applicationContext.getType(name);  
    }  
           
    /** 
     * If the given bean name has aliases in the bean definition, return those aliases.
     * @param name 
     * @return 
     * @throws NoSuchBeanDefinitionException 
     */  
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {  
         return applicationContext.getAliases(name);  
    }  

}
