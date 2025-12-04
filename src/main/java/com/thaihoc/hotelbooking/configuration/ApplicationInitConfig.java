package com.thaihoc.hotelbooking.configuration;

import com.thaihoc.hotelbooking.entity.Role;
import com.thaihoc.hotelbooking.entity.User;
import com.thaihoc.hotelbooking.repository.RoleRepository;
import com.thaihoc.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            if(userRepository.findByEmail("admin").isEmpty()) {

                HashSet<Role> roles = new HashSet<>();

                Optional<Role> optionalRole = roleRepository.findById("ADMIN");
                if (optionalRole.isPresent()) {
                    roles.add(optionalRole.get());

                } else {
                    // xử lý tình huống không tìm thấy role: có thể ném exception hoặc sử dụng role mặc định
                }

                User adminUser = User.builder()
                        .fullName("admin")
                        .email("admin")
                        .roles(roles)
                        .passwordHash(passwordEncoder.encode("admin"))
                        .phone("00000000")
                        .build();

                userRepository.save(adminUser);

                log.info("admin user has been created with default password, please change it");
            }

        };
    }
}
