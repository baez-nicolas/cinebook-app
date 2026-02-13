package com.cinebook.backend.controllers;

import com.cinebook.backend.dtos.auth.AuthResponseDTO;
import com.cinebook.backend.dtos.auth.LoginRequestDTO;
import com.cinebook.backend.dtos.auth.RegisterRequestDTO;
import com.cinebook.backend.services.interfaces.IAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private IAuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequestDTO mockRegisterRequest;
    private LoginRequestDTO mockLoginRequest;
    private AuthResponseDTO mockAuthResponse;

    @BeforeEach
    void setUp() {
        mockRegisterRequest = new RegisterRequestDTO();
        mockRegisterRequest.setEmail("test@example.com");
        mockRegisterRequest.setPassword("password123");
        mockRegisterRequest.setFirstName("John");
        mockRegisterRequest.setLastName("Doe");
        mockRegisterRequest.setPhone("1234567890");

        mockLoginRequest = new LoginRequestDTO();
        mockLoginRequest.setEmail("test@example.com");
        mockLoginRequest.setPassword("password123");

        mockAuthResponse = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test",
                "test@example.com",
                "John",
                "Doe",
                "USER"
        );
    }

    @Test
    @DisplayName("register - Registra usuario exitosamente")
    void register_RegistersSuccessfully() {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponseDTO> response = authController.register(mockRegisterRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        assertEquals("USER", response.getBody().getRole());
        assertNotNull(response.getBody().getToken());

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register - Retorna token JWT en la respuesta")
    void register_ReturnsJWTToken() {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponseDTO> response = authController.register(mockRegisterRequest);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertTrue(response.getBody().getToken().startsWith("eyJ"));

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register - Maneja email duplicado lanzando excepción")
    void register_ThrowsException_WhenEmailExists() {
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RuntimeException("El email ya está registrado"));

        assertThrows(RuntimeException.class, () -> {
            authController.register(mockRegisterRequest);
        });

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register - Acepta teléfono null")
    void register_AcceptsNullPhone() {
        mockRegisterRequest.setPhone(null);
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponseDTO> response = authController.register(mockRegisterRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("register - Retorna rol USER por defecto")
    void register_ReturnsUserRoleByDefault() {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponseDTO> response = authController.register(mockRegisterRequest);

        assertNotNull(response.getBody());
        assertEquals("USER", response.getBody().getRole());

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("login - Inicia sesión exitosamente")
    void login_LogsInSuccessfully() {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponseDTO> response = authController.login(mockLoginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertNotNull(response.getBody().getToken());

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("login - Retorna información completa del usuario")
    void login_ReturnsCompleteUserInfo() {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockAuthResponse);

        ResponseEntity<AuthResponseDTO> response = authController.login(mockLoginRequest);

        AuthResponseDTO body = response.getBody();
        assertNotNull(body);
        assertAll("User info",
            () -> assertNotNull(body.getToken()),
            () -> assertEquals("test@example.com", body.getEmail()),
            () -> assertEquals("John", body.getFirstName()),
            () -> assertEquals("Doe", body.getLastName()),
            () -> assertEquals("USER", body.getRole())
        );

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("login - Lanza excepción con credenciales inválidas")
    void login_ThrowsException_WithInvalidCredentials() {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Email o contraseña incorrectos"));

        assertThrows(RuntimeException.class, () -> {
            authController.login(mockLoginRequest);
        });

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("login - Lanza excepción con usuario desactivado")
    void login_ThrowsException_WithInactiveUser() {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Usuario desactivado"));

        assertThrows(RuntimeException.class, () -> {
            authController.login(mockLoginRequest);
        });

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("login - Lanza excepción con email no registrado")
    void login_ThrowsException_WithUnregisteredEmail() {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Email o contraseña incorrectos"));

        assertThrows(RuntimeException.class, () -> {
            authController.login(mockLoginRequest);
        });

        verify(authService, times(1)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("register - Puede registrar usuario ADMIN")
    void register_CanRegisterAdminUser() {
        AuthResponseDTO adminResponse = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin",
                "admin@example.com",
                "Admin",
                "User",
                "ADMIN"
        );

        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(adminResponse);

        ResponseEntity<AuthResponseDTO> response = authController.register(mockRegisterRequest);

        assertNotNull(response.getBody());
        assertEquals("ADMIN", response.getBody().getRole());

        verify(authService, times(1)).register(any(RegisterRequestDTO.class));
    }

    @Test
    @DisplayName("login - Token diferente para cada login")
    void login_GeneratesDifferentTokens() {
        AuthResponseDTO response1 = new AuthResponseDTO(
                "token1",
                "test@example.com",
                "John",
                "Doe",
                "USER"
        );

        AuthResponseDTO response2 = new AuthResponseDTO(
                "token2",
                "test@example.com",
                "John",
                "Doe",
                "USER"
        );

        when(authService.login(any(LoginRequestDTO.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        ResponseEntity<AuthResponseDTO> firstLogin = authController.login(mockLoginRequest);
        ResponseEntity<AuthResponseDTO> secondLogin = authController.login(mockLoginRequest);

        assertNotEquals(firstLogin.getBody().getToken(), secondLogin.getBody().getToken());

        verify(authService, times(2)).login(any(LoginRequestDTO.class));
    }

    @Test
    @DisplayName("register - Preserva el formato del email")
    void register_PreservesEmailFormat() {
        mockRegisterRequest.setEmail("Test.User@Example.COM");
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(mockAuthResponse);

        authController.register(mockRegisterRequest);

        verify(authService, times(1)).register(argThat(request ->
                request.getEmail().equals("Test.User@Example.COM")
        ));
    }

    @Test
    @DisplayName("login - Verifica que se llame al servicio con los datos correctos")
    void login_CallsServiceWithCorrectData() {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockAuthResponse);

        authController.login(mockLoginRequest);

        verify(authService, times(1)).login(argThat(request ->
                request.getEmail().equals("test@example.com") &&
                request.getPassword().equals("password123")
        ));
    }

    @Test
    @DisplayName("register - Verifica que se llame al servicio con todos los datos")
    void register_CallsServiceWithAllData() {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(mockAuthResponse);

        authController.register(mockRegisterRequest);

        verify(authService, times(1)).register(argThat(request ->
                request.getEmail().equals("test@example.com") &&
                request.getPassword().equals("password123") &&
                request.getFirstName().equals("John") &&
                request.getLastName().equals("Doe") &&
                request.getPhone().equals("1234567890")
        ));
    }
}

