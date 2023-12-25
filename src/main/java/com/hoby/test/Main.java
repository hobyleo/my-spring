package com.hoby.test;

import com.hoby.spring.ApplicationContext;
import com.hoby.test.config.AppConfig;
import com.hoby.test.service.UserService;

/**
 * @author hoby
 * @since 2023-12-15
 */
public class Main {

    public static void main(String[] args) {

        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();

    }
}
