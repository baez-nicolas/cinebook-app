package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.auth.AuthResponseDTO;
import com.cinebook.backend.dtos.auth.LoginRequestDTO;
import com.cinebook.backend.dtos.auth.RegisterRequestDTO;
import com.cinebook.backend.services.interfaces.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints para autenticación")
@CrossOrigin(origins = "*")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("POST /api/auth/register - Email: {}", request.getEmail());
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("POST /api/auth/login - Email: {}", request.getEmail());
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

