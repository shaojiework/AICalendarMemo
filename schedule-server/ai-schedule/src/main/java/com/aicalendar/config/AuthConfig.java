package com.aicalendar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 认证相关Bean配置
 */
@Configuration
public class AuthConfig {

    /**
     * BCrypt密码加密器：用于注册加密与登录校验
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
