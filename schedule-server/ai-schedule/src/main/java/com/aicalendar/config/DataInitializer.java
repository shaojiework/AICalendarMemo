package com.aicalendar.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.aicalendar.entity.User;
import com.aicalendar.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

/**
 * 默认管理员初始化：应用启动时检查，无admin账号则自动创建
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, "admin"));
        if (count > 0) {
            return;
        }
        // 创建默认管理员：admin / admin123（BCrypt加密存储）
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("管理员");
        admin.setRole("ADMIN");
        admin.setStatus(1);
        userMapper.insert(admin);
        log.info("[初始化] 已创建默认管理员: admin / admin123");
    }
}
