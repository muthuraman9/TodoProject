package com.example.todoapp.config;

import com.example.todoapp.model.User;
import com.example.todoapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("muthu").isEmpty()) {
                User muthu = new User();
                muthu.setUsername("muthu");
                muthu.setPassword(passwordEncoder.encode("pass123"));
                userRepository.save(muthu);
            }
            if (userRepository.findByUsername("ravi").isEmpty()) {
                User ravi = new User();
                ravi.setUsername("ravi");
                ravi.setPassword(passwordEncoder.encode("pass123"));
                userRepository.save(ravi);
            }
        };
    }
}
