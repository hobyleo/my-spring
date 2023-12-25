package com.hoby.test.service.impl;

import com.hoby.spring.Autowired;
import com.hoby.spring.BeanNameAware;
import com.hoby.spring.Component;
import com.hoby.spring.InitializingBean;
import com.hoby.test.annotation.Value;
import com.hoby.test.service.OrderService;
import com.hoby.test.service.UserService;

/**
 * @author hoby
 * @since 2023-12-15
 */
@Component("userService")
public class UserServiceImpl implements UserService, InitializingBean, BeanNameAware {

    @Autowired
    private OrderService orderService;

    @Value("123")
    private String value;

    private String beanName;

    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(value);
        System.out.println(beanName);
    }

}
