package com.bookingSystem.config;

import com.bookingSystem.entity.User;
import com.bookingSystem.entity.UserRole;
import com.bookingSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@RequiredArgsConstructor
@Configuration
public class CommandLineRunnerConfig {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner createADMIN() {
        return args -> {

            Optional<User> optional = this.repository.findByRole(UserRole.ADMIN);

            if (optional.isEmpty()) {

                User user = new User();

                user.setUserName(adminUsername);
                user.setPassword(passwordEncoder.encode(adminPassword));
                user.setEmail(adminEmail);
                user.setRole(UserRole.ADMIN);

                this.repository.save(user);
            }
        };
    }
}