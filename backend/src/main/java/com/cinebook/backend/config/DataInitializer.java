package com.cinebook.backend.config;

import com.cinebook.backend.entities.User;
import com.cinebook.backend.entities.enums.UserRole;
import com.cinebook.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createAdminUser();
    }

    private void createAdminUser() {
        String adminEmail = "admin@cinebook.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Usuario admin ya existe");
            return;
        }

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFirstName("Admin");
        admin.setLastName("CineBook");
        admin.setPhone("1234567890");
        admin.setRole(UserRole.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setIsActive(true);

        userRepository.save(admin);

        log.info("Usuario ADMIN creado exitosamente");
        log.info("Email: {}", adminEmail);
        log.info("Password: admin");
    }
}

