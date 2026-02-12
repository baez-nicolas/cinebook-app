package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.UserDTO;
import com.cinebook.backend.entities.User;
import com.cinebook.backend.entities.enums.UserRole;
import com.cinebook.backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Endpoints administrativos")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "Solo accesible para ADMIN")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("📊 Admin consultando lista de usuarios");

        List<UserDTO> users = userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("✅ Total de usuarios: {}", users.size());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Contar usuarios registrados", description = "Solo accesible para ADMIN")
    public ResponseEntity<Map<String, Object>> countUsers() {
        log.info("📊 Admin consultando cantidad de usuarios");

        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.findByRole(UserRole.ADMIN).size();
        long totalRegularUsers = totalUsers - totalAdmins;

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", totalUsers);
        response.put("totalAdmins", totalAdmins);
        response.put("totalRegularUsers", totalRegularUsers);

        log.info("✅ Usuarios: {} (Admins: {}, Users: {})", totalUsers, totalAdmins, totalRegularUsers);

        return ResponseEntity.ok(response);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());
        return dto;
    }
}

