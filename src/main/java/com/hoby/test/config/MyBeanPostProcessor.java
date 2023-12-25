package com.hoby.test.config;

import com.hoby.spring.BeanPostProcessor;
import com.hoby.spring.Component;
import com.hoby.test.annotation.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * Bean 的后置处理器，所有 Bean 初始化时都会调用一遍
 *
 * @author hoby
 * @since 2023-12-16
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                Value value = field.getAnnotation(Value.class);
                field.setAccessible(true);
                try {
                    field.set(bean, value.value());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {

        if ("userService".equals(beanName)) {
            Object proxyInstance = Proxy.newProxyInstance(MyBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), (proxy, method, args) -> {
                // 切面
                System.out.println("切面逻辑");
                return method.invoke(bean, args);
            });
            return proxyInstance;
        }

        return bean;
    }

}
