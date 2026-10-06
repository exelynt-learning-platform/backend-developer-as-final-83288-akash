package com.bookingSystem.config;

import com.bookingSystem.entity.User;
import com.bookingSystem.entity.UserRole;
import com.bookingSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RequiredArgsConstructor
@Configuration
public class CommandLineRunnerConfig {
    public final UserRepository repository;
    public final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createADMIN(){
        return args ->{
            Optional<User> optional = this.repository.findByRole(UserRole.ADMIN);
            if (optional.isEmpty()){
                User user = new User();
                user.setUserName("akash");
                user.setPassword(passwordEncoder.encode("akash@123"));
                user.setEmail("akash@gmail.com");
                user.setRole(UserRole.ADMIN);
                this.repository.save(user);
            }
        };
    }
}
