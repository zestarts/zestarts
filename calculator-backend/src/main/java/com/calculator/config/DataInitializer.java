package com.calculator.config;

import com.calculator.model.Role;
import com.calculator.model.User;
import com.calculator.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User user = new User("user", passwordEncoder.encode("123456"), Role.ROLE_USER);
        userRepository.save(user);

        User vip = new User("vip", passwordEncoder.encode("123456"), Role.ROLE_VIP);
        userRepository.save(vip);

        User admin = new User("admin", passwordEncoder.encode("123456"), Role.ROLE_ADMIN);
        userRepository.save(admin);

        System.out.println("=== 测试账号初始化完成 ===");
        System.out.println("普通用户: user / 123456");
        System.out.println("VIP用户:  vip  / 123456");
        System.out.println("管理员:   admin / 123456");
        System.out.println("=========================");
    }
}