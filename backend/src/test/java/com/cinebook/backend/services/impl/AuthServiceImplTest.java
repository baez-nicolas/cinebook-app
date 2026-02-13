package com.cinebook.backend.services.impl;

import com.cinebook.backend.dtos.auth.AuthResponseDTO;
import com.cinebook.backend.dtos.auth.LoginRequestDTO;
import com.cinebook.backend.dtos.auth.RegisterRequestDTO;
import com.cinebook.backend.entities.User;
import com.cinebook.backend.entities.enums.UserRole;
import com.cinebook.backend.repositories.UserRepository;
import com.cinebook.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private User mockUser;
    private String mockToken;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhone("1234567890");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPhone("1234567890");
        mockUser.setRole(UserRole.USER);
        mockUser.setCreatedAt(LocalDateTime.now());
        mockUser.setIsActive(true);

        mockToken = "mockJwtToken123";
    }

    @Test
    @DisplayName("Register - Usuario nuevo se registra exitosamente")
    void register_NewUser_Success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRole().name())).thenReturn(mockToken);

        AuthResponseDTO response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(mockToken, response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("USER", response.getRole());

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequest.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(mockUser.getEmail(), mockUser.getRole().name());
    }

    @Test
    @DisplayName("Register - Email ya existe, lanza excepción")
    void register_EmailAlreadyExists_ThrowsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("El email ya está registrado", exception.getMessage());

        verify(userRepository, times(1)).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Register - Usuario se crea con rol USER por defecto")
    void register_NewUser_DefaultRoleIsUser() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(UserRole.USER, savedUser.getRole());
            assertEquals(true, savedUser.getIsActive());
            assertNotNull(savedUser.getCreatedAt());
            return savedUser;
        });
        when(jwtService.generateToken(anyString(), anyString())).thenReturn(mockToken);

        authService.register(registerRequest);

        verify(userRepository, times(1)).save(argThat(user ->
                user.getRole() == UserRole.USER &&
                user.getIsActive() == true &&
                user.getCreatedAt() != null
        ));
    }

    @Test
    @DisplayName("Register - Password se encripta correctamente")
    void register_NewUser_PasswordIsEncoded() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals("encodedPassword", savedUser.getPassword());
            return savedUser;
        });
        when(jwtService.generateToken(anyString(), anyString())).thenReturn(mockToken);

        authService.register(registerRequest);

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(argThat(user ->
                "encodedPassword".equals(user.getPassword())
        ));
    }

    @Test
    @DisplayName("Login - Credenciales válidas, login exitoso")
    void login_ValidCredentials_Success() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRole().name())).thenReturn(mockToken);

        AuthResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals(mockToken, response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("USER", response.getRole());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), mockUser.getPassword());
        verify(jwtService, times(1)).generateToken(mockUser.getEmail(), mockUser.getRole().name());
    }

    @Test
    @DisplayName("Login - Usuario no existe, lanza excepción")
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Email o contraseña incorrectos", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Password incorrecto, lanza excepción")
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Email o contraseña incorrectos", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), mockUser.getPassword());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Usuario desactivado, lanza excepción")
    void login_UserInactive_ThrowsException() {
        mockUser.setIsActive(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Usuario desactivado", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), mockUser.getPassword());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Login - Usuario ADMIN puede hacer login")
    void login_AdminUser_Success() {
        mockUser.setRole(UserRole.ADMIN);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(mockUser.getEmail(), "ADMIN")).thenReturn(mockToken);

        AuthResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());

        verify(jwtService, times(1)).generateToken(mockUser.getEmail(), "ADMIN");
    }

    @Test
    @DisplayName("Register - Datos completos se guardan correctamente")
    void register_AllFieldsSavedCorrectly() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals("test@example.com", savedUser.getEmail());
            assertEquals("John", savedUser.getFirstName());
            assertEquals("Doe", savedUser.getLastName());
            assertEquals("1234567890", savedUser.getPhone());
            return savedUser;
        });
        when(jwtService.generateToken(anyString(), anyString())).thenReturn(mockToken);

        authService.register(registerRequest);

        verify(userRepository, times(1)).save(argThat(user ->
                "test@example.com".equals(user.getEmail()) &&
                "John".equals(user.getFirstName()) &&
                "Doe".equals(user.getLastName()) &&
                "1234567890".equals(user.getPhone())
        ));
    }

    @Test
    @DisplayName("Login - JWT token se genera con email y rol correctos")
    void login_JwtTokenGeneratedWithCorrectData() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRole().name())).thenReturn(mockToken);

        authService.login(loginRequest);

        verify(jwtService, times(1)).generateToken("test@example.com", "USER");
    }

    @Test
    @DisplayName("Register - JWT token se genera después de guardar usuario")
    void register_JwtTokenGeneratedAfterSave() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRole().name())).thenReturn(mockToken);

        AuthResponseDTO response = authService.register(registerRequest);

        assertNotNull(response.getToken());
        assertEquals(mockToken, response.getToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(mockUser.getEmail(), mockUser.getRole().name());
    }
}

