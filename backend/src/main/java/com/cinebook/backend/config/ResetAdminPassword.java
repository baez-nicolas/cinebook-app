package com.cinebook.backend.config;

import com.cinebook.backend.entities.User;
import com.cinebook.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(10)
public class ResetAdminPassword implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String newPassword = System.getenv("ADMIN_RESET_PASSWORD");

        if (newPassword != null && !newPassword.isEmpty()) {
            log.info("Variable ADMIN_RESET_PASSWORD detectada. Iniciando reset de password...");

            User admin = userRepository.findByEmail("admin@cinebook.com")
                    .orElse(null);

            if (admin != null) {
                admin.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(admin);
                log.info("Password del admin actualizada exitosamente");
            } else {
                log.error("Usuario admin@cinebook.com no encontrado");
            }
        } else {
            log.info("Variable ADMIN_RESET_PASSWORD no encontrada. Saltando reset de password.");
        }
    }
}

